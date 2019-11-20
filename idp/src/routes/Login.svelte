<script>
    import {user} from "../stores/user";
    import {validEmail} from "../validation/regexp";
    import I18n from "i18n-js";
    import {magicLinkNewUser, magicLinkExistingUser} from "../api/index";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";

    export let id;
    let emailInUse = false;
    let emailNotFound = false;
    let sessionExpired = false;
    let initial = true;

    let passwordField;

    onMount(() => {
        const value = Cookies.get("login_preference");
        $user.usePassword = value === "usePassword";
    });

    const handleNext = passwordFlow => () => {
        if (($user.usePassword && passwordFlow) || (!$user.usePassword && !passwordFlow)) {
            if (allowedNext($user.email, $user.givenName, $user.familyName)) {
                if ($user.createAccount) {
                    magicLinkNewUser($user.email, $user.givenName, $user.familyName, $user.rememberMe, id)
                            .then(() => navigate(`/magic/${id}`, {replace: true}))
                            .catch(e => {
                                if (e.status === 410) {
                                    sessionExpired = true;
                                } else {
                                    emailInUse = true;
                                    emailNotFound = false;
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
                        if (e.status === 410) {
                            sessionExpired = true;
                        } else {
                            emailNotFound = true;
                            emailInUse = false;
                        }
                    });
                }
            } else {
                initial = false;
            }
        } else {
            $user.usePassword = passwordFlow;
        }
    };

    const init = el => el.focus();

    const allowedNext = (email, familyName, givenName) => {
        return $user.createAccount ? validEmail(email) && familyName && givenName : validEmail(email);
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

    const handleLinkClick = e => e.key === " " && e.target.click();

</script>

<style>
    .home {
        display: flex;
        justify-content: center;
        width: 100%;
        font-size: 18px;
    }

    .card {
        display: flex;
        flex-direction: column;
        padding: 50px;
        border-radius: 4px;
        background-color: white;
        width: 500px;
    }

    h2 {
        margin-bottom: 20px;
    }

    h2.top {
        margin-bottom: 40px;
    }

    .options {
        display: flex;
        justify-content: space-between;
        width: 100%;
        padding-bottom: 30px;
        border-bottom: 2px solid #dadce0;
    }

    .button {
        border: 1px solid #818181;
        width: 100%;
        background-color: #c7c7c7;
        border-radius: 2px;
        padding: 10px 20px;
        display: inline-block;
        color: black;
        text-decoration: none;
        cursor: pointer;
        text-align: center;
    }

    .button.non-active {
        border: 1px solid #c5c5c5;
        color: #818181;
        background-color: white;
    }

    .button:not(.non-active) {
        order: 1;
        margin-left: 20px;
    }

    .button.disabled {
        border: none;
        cursor: not-allowed;
        color: #c7c7c7;
        background-color: #f3f3f3;
    }

    .button.full {
        margin-top: 15px;
        margin-left: 0;
    }

    span.error {
        display: inline-block;
        margin: 0 auto 10px 0;
        color: #d00000;
    }

    input[type=email], input[type=text], input[type=password] {
        border: 1px solid #dadce0;
        border-radius: 4px;
        padding: 10px;
        font-size: larger;
        width: 100%;
        margin: 5px 0;
    }

    .hidden {
        display: none;
    }

    h2, label {
        color: #767676
    }

    .pre-input-label {
        font-weight: bold;
    }

    .post-input-label {
        font-size: 16px;
        margin: 5px 0 20px 0;
        display: inline-block;
    }

    div.checkbox {
        display: flex;
        margin-right: auto;
        align-items: center;
        margin-bottom: 15px;
    }

    div.checkbox input {
        margin: 0 10px 0 0;
        font-size: 36px;
        cursor: pointer;
    }

    div.checkbox label {
        cursor: pointer;
    }

    div.info-bottom {
        margin-top: 30px;
    }

    div.info-bottom p {
        display: inline-block;
        margin-bottom: 10px;
    }
</style>
<div class="home">
    <div class="card">
        <h2 class="top">{@html I18n.t("login.header")}</h2>
        <label class="pre-input-label">{I18n.t("login.email")}</label>
        <input type="email"
               use:init
               bind:value={$user.email}
               on:keydown={handleEmailEnter}>
        {#if !$user.createAccount && emailNotFound}
            <span class="error">{I18n.t("login.emailNotFound")}</span>
        {/if}
        {#if !initial && !validEmail($user.email)}
            <span class="error">{I18n.t("login.invalidEmail")}</span>
        {/if}
        {#if $user.createAccount && emailInUse}
            <span class="error">{I18n.t("login.emailInUse")}</span>
        {/if}
        {#if !$user.usePassword}
            <label class="post-input-label">{I18n.t("login.magicLinkText")}</label>
        {/if}
        {#if $user.createAccount}
            <label class="pre-input-label">{I18n.t("login.givenName")}</label>
            <input type="text"
                   placeholder={I18n.t("login.givenNamePlaceholder")}
                   bind:value={$user.givenName}>
            {#if !initial && !$user.givenName}
                <span class="error">{I18n.t("login.requiredAttribute", {attr: I18n.t("login.givenName")})}</span>
            {/if}
            <label class="pre-input-label">{I18n.t("login.familyName")}</label>
            <input type="text"
                   placeholder={I18n.t("login.familyNamePlaceholder")}
                   bind:value={$user.familyName}>
            {#if !initial && !$user.familyName}
                <span class="error">{I18n.t("login.requiredAttribute", {attr: I18n.t("login.familyName")})}</span>
            {/if}
            <div class="options">
                <a class="button full" href="/magic"
                   class:disabled={!initial && !allowedNext($user.email, $user.familyName, $user.givenName)}
                   on:keydown={handleLinkClick}
                   on:click|preventDefault|stopPropagation={handleNext(false)}>
                    {I18n.t("login.sendMagicLink")}
                </a>
            </div>
        {:else}
            <div id="password" class:hidden={!$user.usePassword}>
                <label class="pre-input-label">{I18n.t("login.password")}</label>
                <input type="password"
                       on:keydown={handlePasswordEnter}
                       bind:value={$user.password}
                       bind:this={passwordField}>
                <label class="post-input-label">{I18n.t("login.passwordForgotten")}</label>
            </div>
            <div class="checkbox">
                <input type="checkbox"
                       id="remember-me"
                       bind:checked={$user.rememberMe}/>
                <label for="remember-me">{I18n.t("login.rememberMe")}</label>
            </div>
            <div class="options">
                <a class="button child" class:non-active={!$user.usePassword} href="/password"
                   class:disabled={!initial && !allowedNext($user.email, $user.familyName, $user.givenName) && $user.usePassword}
                   on:click|preventDefault|stopPropagation={handleNext(true)}>
                    {$user.usePassword ?  I18n.t("login.login") : I18n.t("login.usePassword")}
                </a>
                <a class="button child" class:non-active={$user.usePassword} href="/magic"
                   class:disabled={!initial && !allowedNext($user.email, $user.familyName, $user.givenName) && !$user.usePassword}
                   on:click|preventDefault|stopPropagation={handleNext(false)}>
                    {$user.usePassword ?  I18n.t("login.useMagicLink") : I18n.t("login.sendMagicLink")}
                </a>
            </div>
        {/if}
        <div class="info-bottom">
            {#if !$user.createAccount}
                <h2>{@html I18n.t("login.noGuestAccount")}</h2>
                <p>{@html I18n.t("login.noGuestAccountInfo")}</p>
                <a href="/reguest"
                   on:click|preventDefault|stopPropagation={createAccount(true)}>{I18n.t("login.requestGuestAccount")}</a>
            {:else}
                <h2>{@html I18n.t("login.alreadyGuestAccount")}</h2>
                <a href="/login"
                   on:click|preventDefault|stopPropagation={createAccount(false)}>{I18n.t("login.login")}</a>

            {/if}
        </div>

    </div>
</div>