<script>
    import {user} from "../stores/user";
    import {validEmail} from "../validation/regexp";
    import I18n from "i18n-js";
    import {magicLinkNewUser, magicLinkExistingUser} from "../api/index";
    import CheckBox from "../components/CheckBox.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import Button from "../components/Button.svelte";

    export let id;
    let emailInUse = false;
    let emailNotFound = false;
    let emailOrPasswordIncorrect = false;
    let initial = true;
    let showSpinner = false;
    let serviceName = "";

    let passwordField;

    const intervalId = setInterval(() => {
        const value = (passwordField || {}).value;
        if (value && !$user.usePassword) {
            $user.usePassword = true;
            clearInterval(intervalId);
        }
    }, 750);

    onMount(() => {
        const value = Cookies.get("login_preference");
        $user.usePassword = value === "usePassword";
        const urlParams = new URLSearchParams(window.location.search);
        serviceName = urlParams.get("name")
    });



    const handleNext = passwordFlow => () => {
        if (($user.usePassword && passwordFlow) || (!$user.usePassword && !passwordFlow)) {
            if (allowedNext($user.email, $user.givenName, $user.familyName, $user.password)) {
                showSpinner = true;
                if ($user.createAccount) {
                    magicLinkNewUser($user.email, $user.givenName, $user.familyName, $user.rememberMe, id)
                            .then(() => navigate(`/magic/${id}`, {replace: true}))
                            .catch(e => {
                                showSpinner = false;
                                if (e.status === 409) {
                                    emailInUse = true;
                                    emailNotFound = false;
                                    emailOrPasswordIncorrect = false;
                                } else {
                                    navigate("/expired", {replace: true});
                                }
                            });
                } else {
                    magicLinkExistingUser($user.email, $user.password, $user.rememberMe, $user.usePassword, id)
                            .then(json => {
                                Cookies.set("login_preference", $user.usePassword ? "usePassword" : "useLink", {expires: 365});
                                if ($user.usePassword) {
                                    window.location.href = json.url
                                } else {
                                    navigate(`/magic/${id}`, {replace: true});
                                }
                            }).catch(e => {
                        showSpinner = false;
                        if (e.status === 404) {
                            if ($user.usePassword) {
                                emailNotFound = false;
                                emailOrPasswordIncorrect = true;
                                emailInUse = false;
                            } else {
                                emailNotFound = true;
                                emailOrPasswordIncorrect = false;
                                emailInUse = false;
                            }
                        } else if (e.status === 403) {
                            emailNotFound = false;
                            emailOrPasswordIncorrect = true;
                            emailInUse = false;

                        } else {
                            navigate("/expired", {replace: true});
                        }
                    });
                }
            } else {
                initial = false;
            }
        } else {
            $user.usePassword = passwordFlow;
            if (!passwordFlow) {
                $user.password = "";
            }
        }
    };

    const init = el => el.focus();

    const allowedNext = (email, familyName, givenName, password) => {
        return $user.createAccount ? validEmail(email) && familyName && givenName :
                $user.usePassword ? validEmail(email) && password : validEmail(email);
    };

    const createAccount = newAccount => () => {
        $user.createAccount = newAccount;
        $user.usePassword = false;
        $user.givenName = "";
        $user.familyName = "";
        emailInUse = false;
        emailNotFound = false;
    };

    const handleEmailEnter = e => {
        if (e.key === "Enter") {
            if (!$user.createAccount && !$user.usePassword) {
                handleNext(false)();
            } else if ($user.usePassword) {
                passwordField.focus();
            }
        }
    };

    const handlePasswordEnter = e => e.key === "Enter" && handleNext(true)();

</script>

<style>

    h2.top {
        margin: 6px 0 35px 0;
        color: var(--color-primary-green);
    }

    h3 {
        margin-bottom: 15px;
    }

    .options {
        display: flex;
        flex-direction: column;
        width: 100%;
        padding: 25px 0 30px 0;
        border-bottom: 2px solid #5bd685;
    }

    span.error {
        display: inline-block;
        margin: 0 auto 10px 0;
        color: var(--color-primary-red);
    }

    input[type=email], input[type=text], input[type=password] {
        border: 1px solid #727272;
        border-radius: 4px;
        padding: 10px;
        font-size: 18px;
        width: 100%;
        margin: 8px 0 15px 0;
    }

    .hidden {
        display: none;
    }

    .pre-input-label {
        color: var(--color-primary-black);
        font-weight: 600;
    }

    div.password-option {
        margin-top: 20px;
    }
    div.info-bottom {
        margin-top: 30px;
    }

    div.info-bottom p {
        display: inline-block;
        margin-bottom: 20px;
    }

    a.toggle-link {
        font-family: Proxima Nova, sans-serif;
        text-decoration: none;
        font-size: 18px;
        line-height: 20px;
        font-weight: bold;
        color: #0077c8;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}

<h1>{@html I18n.ts("login.header")}</h1>
<h2 class="top">{@html I18n.ts("login.header2", {name: serviceName})}</h2>
<label class="pre-input-label">{I18n.ts("login.email")}</label>
<input type="email"
       use:init
       bind:value={$user.email}
       on:keydown={handleEmailEnter}>
{#if !$user.createAccount && emailNotFound}
    <span class="error">{I18n.ts("login.emailNotFound")}</span>
{/if}
{#if $user.usePassword && emailOrPasswordIncorrect}
    <span class="error">{I18n.ts("login.emailOrPasswordIncorrect")}</span>
{/if}
{#if !initial && !validEmail($user.email)}
    <span class="error">{I18n.ts("login.invalidEmail")}</span>
{/if}
{#if $user.createAccount && emailInUse}
    <span class="error">{I18n.ts("login.emailInUse")}</span>
{/if}
{#if $user.createAccount}
    <label class="pre-input-label">{I18n.ts("login.givenName")}</label>
    <input type="text"
           placeholder={I18n.ts("login.givenNamePlaceholder")}
           bind:value={$user.givenName}>
    {#if !initial && !$user.givenName}
        <span class="error">{I18n.ts("login.requiredAttribute", {attr: I18n.ts("login.givenName")})}</span>
    {/if}
    <label class="pre-input-label">{I18n.ts("login.familyName")}</label>
    <input type="text"
           placeholder={I18n.ts("login.familyNamePlaceholder")}
           bind:value={$user.familyName}>
    {#if !initial && !$user.familyName}
        <span class="error">{I18n.ts("login.requiredAttribute", {attr: I18n.ts("login.familyName")})}</span>
    {/if}
    <div class="options">
        <Button disabled={showSpinner || !allowedNext($user.email, $user.familyName, $user.givenName, $user.password)}
                active={allowedNext($user.email, $user.familyName, $user.givenName, $user.password)}
                href="/magic"
                className="full"
                label={I18n.ts("login.sendMagicLink")}
                onClick={handleNext(false)}/>
    </div>
{:else}
    <div id="password" class:hidden={!$user.usePassword}>
        <label class="pre-input-label">{I18n.ts("login.password")}</label>
        <input type="password" id="password-field"
               on:keydown={handlePasswordEnter}
               bind:value={$user.password}
               bind:this={passwordField}
                >
    </div>
    <CheckBox value={$user.rememberMe}
              label={I18n.ts("login.rememberMe")}
              name="remember-me"
              onChange={val => $user.rememberMe = val}/>
    <div class="options">
        {#if $user.usePassword}
            <Button href={`/${$user.usePassword ?  I18n.ts("login.login") : I18n.ts("login.usePassword")}`}
                    disabled={showSpinner ||!allowedNext($user.email, $user.familyName, $user.givenName, $user.password) && $user.usePassword}
                    label={$user.usePassword ?  I18n.ts("login.login") : I18n.ts("login.usePassword")}
                    onClick={handleNext(true)}/>
            <div class="password-option">
                <span>{I18n.ts("login.passwordForgotten")}</span>
                <a href={I18n.ts("login.login")}
                   on:click|preventDefault|stopPropagation={handleNext(false)}>{I18n.ts("login.passwordForgottenLink")}</a>
            </div>


        {:else}
            <Button href="/magic"
                    disabled={showSpinner ||!allowedNext($user.email, $user.familyName, $user.givenName, $user.password) && !$user.usePassword}
                    label={$user.usePassword ?  I18n.ts("login.useMagicLink") : I18n.ts("login.sendMagicLink")}
                    onClick={handleNext(false)}/>
            <div class="password-option">
                <span>{I18n.t("login.noPasswordNeeded")}</span>
                <a href={I18n.ts("login.usePassword")}
                   on:click|preventDefault|stopPropagation={handleNext(true)}>{I18n.ts("login.usePasswordLink")}</a>
            </div>
        {/if}
    </div>
{/if}
<div class="info-bottom">
    {#if !$user.createAccount}
        <h3>{@html I18n.ts("login.noGuestAccount")}</h3>
        <p>{@html I18n.ts("login.noGuestAccountInfo")}</p>
        <a class="toggle-link" href="/reguest"
           on:click|preventDefault|stopPropagation={createAccount(true)}>{I18n.ts("login.requestGuestAccount")}</a>
    {:else}
        <h3>{@html I18n.ts("login.alreadyGuestAccount")}</h3>
        <a class="toggle-link" href="/login"
           on:click|preventDefault|stopPropagation={createAccount(false)}>{I18n.ts("login.login")}</a>

    {/if}
</div>
