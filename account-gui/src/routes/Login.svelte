<script>
    import {user} from "../stores/user";
    import {validEmail} from "../validation/regexp";
    import I18n from "i18n-js";
    import critical from "../icons/critical.svg"
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
    let agreedWithTerms = false;

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
        serviceName = urlParams.get("name");

        const modus = urlParams.get("modus");
        if (modus && modus === "cr") {
            $user.createAccount = true;
        }

    });

    const handleNext = passwordFlow => () => {
        if (($user.usePassword && passwordFlow) || (!$user.usePassword && !passwordFlow)) {
            if (allowedNext($user.email, $user.givenName, $user.familyName, $user.password, agreedWithTerms)) {
                showSpinner = true;
                const modus = $user.createAccount ? "cr" : "ea";
                if ($user.createAccount) {
                    magicLinkNewUser($user.email, $user.givenName, $user.familyName, $user.rememberMe, id)
                            .then(() => navigate(`/magic/${id}?name=${encodeURIComponent(serviceName)}&modus=${modus}`, {replace: true}))
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
                                Cookies.set("login_preference", $user.usePassword ? "usePassword" : "useLink", {
                                    expires: 365,
                                    secure: true,
                                    sameSite: "Lax"
                                });
                                if ($user.usePassword) {
                                    window.location.href = json.url
                                } else {
                                    navigate(`/magic/${id}?name=${encodeURIComponent(serviceName)}&modus=${modus}`, {replace: true});
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

    const allowedNext = (email, familyName, givenName, password, agreedWithTerms) => {
        return $user.createAccount ? validEmail(email) && familyName && givenName && agreedWithTerms :
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

    const clearGivenName = () => $user.givenName = $user.givenName.replace(/[<>]/g, "");
    const clearFamilyName = () => $user.familyName = $user.familyName.replace(/[<>]/g, "");

</script>

<style>

    h2.header {
        font-weight: 300;
    }

    h2.top {
        margin: 6px 0 30px 0;
        color: var(--color-primary-green);
        font-size: 28px;
    }

    h3.mixed {
        font-weight: normal;
    }

    .options {
        display: flex;
        flex-direction: column;
        width: 100%;
        padding: 5px 0 20px 0;
        border-bottom: 1px solid #979797;
    }

    div.error {
        display: flex;
        align-items: center;
        color: var(--color-primary-red);
        margin-bottom: 10px;
    }

    div.error span.svg {
        display: inline-block;
        margin-right: 10px;
    }

    span.error {
        display: inline-block;
        margin: 0 auto 10px 0;
        color: var(--color-primary-red);
    }

    input[type=email], input[type=text], input[type=password] {
        border: 1px solid #727272;
        border-radius: 6px;
        padding: 14px;
        font-size: 16px;
        width: 100%;
        margin: 8px 0 15px 0;
    }

    input.error {
        border: solid 1px var(--color-primary-red);
        background-color: #fff5f3;
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

    span.no-password-needed {
        display: inline-block;
        margin-bottom: 10px;
    }

    div.info-bottom {
        margin-top: 15px;
        padding-left: 10px;
    }

    a {
        text-decoration: underline;
    }

    a.toggle-link {
        font-size: 14px;
        line-height: 2.42;
        color: #0062b0;
        display: block;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
{#if $user.createAccount}
    <h2 class="header">{I18n.ts("login.header2")}</h2>
    <h2 class="top">{I18n.ts("login.header3")}</h2>
{:else}
    <h2 class="header">{I18n.ts("login.header")}</h2>
    <h2 class="top">{serviceName}</h2>
{/if}
<label for="email" class="pre-input-label">{I18n.ts("login.email")}</label>
<input type="email"
       autocomplete="username"
       id="email"
       class={`${emailNotFound || emailOrPasswordIncorrect || emailInUse ? 'error' : ''}`}
       placeholder={I18n.ts("login.emailPlaceholder")}
       use:init
       bind:value={$user.email}
       on:keydown={handleEmailEnter}>
{#if !$user.createAccount && emailNotFound}
    <div class="error"><span class="svg">{@html critical}</span><span>{I18n.ts("login.emailNotFound")}</span></div>
{/if}
{#if $user.usePassword && emailOrPasswordIncorrect}
    <div class="error"><span class="svg">{@html critical}</span><span>{I18n.ts("login.emailOrPasswordIncorrect")}</span></div>
{/if}
{#if !initial && !validEmail($user.email)}
    <div class="error"><span class="svg">{@html critical}</span><span>{I18n.ts("login.invalidEmail")}</span></div>
{/if}
{#if $user.createAccount && emailInUse}
    <div class="error"><span class="svg">{@html critical}</span><span>{I18n.ts("login.emailInUse")}</span></div>
{/if}
{#if $user.createAccount}
    <label for="given-name" class="pre-input-label">{I18n.ts("login.givenName")}</label>
    <input type="text"
           id="given-name"
           placeholder={I18n.ts("login.givenNamePlaceholder")}
           value={$user.givenName}
           bind:value={$user.givenName}
           on:change={clearGivenName}>
    {#if !initial && !$user.givenName}
        <span class="error">{I18n.ts("login.requiredAttribute", {attr: I18n.ts("login.givenName")})}</span>
    {/if}
    <label for="family-name" class="pre-input-label">{I18n.ts("login.familyName")}</label>
    <input type="text"
           id="family-name"
           placeholder={I18n.ts("login.familyNamePlaceholder")}
           value={$user.familyName}
           bind:value={$user.familyName}
           on:change={clearFamilyName}>
    {#if !initial && !$user.familyName}
        <span class="error">{I18n.ts("login.requiredAttribute", {attr: I18n.ts("login.familyName")})}</span>
    {/if}
    <CheckBox value={agreedWithTerms}
              className="light"
              label={I18n.ts("login.agreeWithTerms")}
              name="agreeWithTerms"
              onChange={val => agreedWithTerms = val}/>
    <div class="options">
        <Button disabled={showSpinner || !allowedNext($user.email, $user.familyName, $user.givenName, $user.password, agreedWithTerms)}
                href="/magic"
                className="full"
                label={$user.createAccount ? I18n.ts("login.requestEduId"): I18n.ts("login.sendMagicLink")}
                onClick={handleNext(false)}/>
    </div>
{:else}
    <div id="password" class:hidden={!$user.usePassword}>
        <label for="password-field" class="pre-input-label">{I18n.ts("login.password")}</label>
        <input type="password"
               autocomplete="current-password"
               id="password-field"
               on:keydown={handlePasswordEnter}
               bind:value={$user.password}
               bind:this={passwordField}>
    </div>

    {#if !$user.usePassword}
        <span class="no-password-needed">{I18n.t("login.noPasswordNeeded")}</span>
    {/if}

    <CheckBox value={$user.rememberMe}
              label={I18n.ts("login.rememberMe")}
              name="remember-me"
              onChange={val => $user.rememberMe = val}/>
    <div class="options">
        {#if $user.usePassword}
            <Button href={`/${$user.usePassword ?  I18n.ts("login.login") : I18n.ts("login.usePassword")}`}
                    disabled={showSpinner ||!allowedNext($user.email, $user.familyName, $user.givenName, $user.password, true) && $user.usePassword}
                    label={$user.usePassword ?  I18n.ts("login.login") : I18n.ts("login.usePassword")}
                    className="full"
                    onClick={handleNext(true)}/>
            <div class="password-option">
                <span>{I18n.ts("login.passwordForgotten")}</span>
                <a href={I18n.ts("login.login")}
                   on:click|preventDefault|stopPropagation={handleNext(false)}>{I18n.ts("login.passwordForgottenLink")}</a>
            </div>
        {:else}
            <Button href="/magic"
                    disabled={showSpinner ||!allowedNext($user.email, $user.familyName, $user.givenName, $user.password, true) && !$user.usePassword}
                    label={$user.usePassword ?  I18n.ts("login.useMagicLink") : I18n.ts("login.sendMagicLink")}
                    className="full"
                    onClick={handleNext(false)}/>
            <div class="password-option">
                <a href={I18n.ts("login.usePassword")}
                   on:click|preventDefault|stopPropagation={handleNext(true)}>{I18n.ts("login.usePasswordLink")}</a>
                <span>{I18n.t("login.usePasswordLinkInfo")}</span>
            </div>
        {/if}
    </div>
{/if}
<div class="info-bottom">
    {#if !$user.createAccount}
        <h3 class="mixed">{@html I18n.ts("login.noGuestAccount")}</h3>
        <a class="toggle-link" href="/reguest"
           on:click|preventDefault|stopPropagation={createAccount(true)}>{I18n.ts("login.requestGuestAccount")}</a>
    {:else}
        <h3 class="mixed">{@html I18n.ts("login.alreadyGuestAccount")}</h3>
        <a class="toggle-link" href="/login"
           on:click|preventDefault|stopPropagation={createAccount(false)}>{I18n.ts("login.login")}</a>

    {/if}

    <a class="toggle-link" target="_blank" href="https://eduid.nl">{I18n.ts("login.whatis")}</a>
</div>
