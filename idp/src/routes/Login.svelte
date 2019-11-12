<script>
    import logo from "./logo.svg";
    import {user} from "../stores/user";
    import {validEmail} from "../validation/regexp";
    import I18n from "i18n-js";
    import {magicLinkNewUser, magicLinkExistingUser} from "../api/index";
    import {navigate} from "svelte-routing";

    export let id;
    let unknownUser = false;
    let usePassword = false;
    let emailInUse = false;
    let emailNotFound = false;

    const handleNext = () => {
        if (unknownUser) {
            magicLinkNewUser($user.email, $user.givenName, $user.familyName, $user.rememberMe, id)
                    .then(() => {
                        navigate("/magic", {replace: true});
                    }).catch(() => {
                emailInUse = true;
                emailNotFound = false;
            });
        } else {
            magicLinkExistingUser($user.email, $user.password, $user.rememberMe, id, usePassword)
                    .then(() => {
                        navigate("/magic", {replace: true});
                    }).catch(() => {
                emailNotFound = true;
                emailInUse = false;
            });
        }
    };

    const togglePassword = usePasswordRef => {
        const p = document.getElementById("password");
        if (p) {
            p.classList.toggle("hidden")
        }

    };

    $: togglePassword(usePassword) ;

    const allowedNext = (email, familyName, givenName) => {
        return unknownUser ?
                validEmail(email) && familyName && givenName : validEmail(email);
    };

    const createAccount = () => {
        unknownUser = !unknownUser;
        emailInUse = false;
        emailNotFound = false;
        $user.givenName = "";
        $user.familyName = "";
    }

</script>

<style>
    .home {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
    }

    .card {
        display: flex;
        flex-direction: column;
        border: 1px solid #dadce0;
        border-radius: 4px;
        background-color: white;
        height: auto;
        min-height: 500px;
        width: auto;
        min-width: 500px;
        max-width: 500px;
    }

    .container {
        display: flex;
        flex-direction: column;
        align-items: center;
        height: 100%;
        padding: 25px;
    }

    .logo {
        padding: 40px;
        margin-top: auto;
    }

    :global(.logo) svg {
        height: 51px;
        width: 122px;
    }

    .buttons {
        display: flex;
        margin: auto 15px 15px 15px;
        align-items: center;
    }

    button {
        border-radius: 4px;
        padding: 10px 20px;
        display: inline-block;
        font-size: larger;
        background-color: #5da7c5;
        color: whitesmoke;
    }

    button:hover {
        cursor: pointer;
    }

    a.create-link {
        margin-right: auto;
    }

    button.disabled {
        cursor: not-allowed;
        color: #C5C5C5;
        background-color: whitesmoke;
    }

    span.new-to {
        margin-right: 5px;
    }

    span.error {
        display: inline-block;
        margin: 0 auto 10px 0;
        color: #d00000;
    }

    span.info {
        display: inline-block;
        margin: 0 auto 10px 0;
        color: #767676;
        font-size: 14px;
    }

    input[type=email], input[type=text], input[type=password] {
        border: 1px solid #dadce0;
        border-radius: 4px;
        padding: 10px;
        font-size: larger;
        width: 100%;
        margin: 20px 10px;
    }

    input[type=password].hidden {
        display: none;
    }

    h2, label {
        color: #767676
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
</style>
<div class="home">
    <div class="card">
        <div class="container">
            <div class="logo">
                {@html logo}
            </div>
            <h2>{I18n.t("login.header")}</h2>
            <input type="email"
                   placeholder={I18n.t("login.emailPlaceholder")}
                   bind:value={$user.email}
                   on:keydown={e => e.key === "Enter" && handleNext()}>
            {#if !unknownUser && emailNotFound}
                <span class="error">{I18n.t("login.emailNotFound")}</span>
            {/if}
            {#if unknownUser && emailInUse}
                <span class="error">{I18n.t("login.emailInUse")}</span>
            {/if}
            {#if unknownUser}
                <input type="text"
                       placeholder={I18n.t("login.giveNamePlaceholder")}
                       bind:value={$user.givenName}>
                <input type="text"
                       placeholder={I18n.t("login.familyNamePlaceholder")}
                       bind:value={$user.familyName}>
            {:else}
                    <input class="hidden"
                           type="password"
                           id="password"
                           placeholder={I18n.t("login.passwordPlaceholder")}
                           bind:value={$user.password}>
                {#if usePassword}
                    <span class="info">{I18n.t("login.passwordDisclaimer")}</span>
                {/if}
                <div class="checkbox">
                    <input type="radio"
                           id="use-magic-link"
                           bind:group={usePassword}
                           value={false}/>
                    <label for="use-magic-link">{I18n.t("login.useMagicLink")}</label>
                </div>
                <div class="checkbox">
                    <input type="radio"
                           id="use-password"
                           bind:group={usePassword}
                           value={true}/>
                    <label for="use-password">{I18n.t("login.usePassword")}</label>
                </div>
            {/if}
            <div class="checkbox">
                <input type="checkbox"
                       id="remember-me"
                       bind:checked={$user.rememberMe}/>
                <label for="remember-me">{I18n.t("login.rememberMe")}</label>
            </div>
        </div>
        <div class="buttons">
            {#if !unknownUser}
                <span class="new-to">{I18n.t("login.newTo")}</span>
            {/if}
            <a class="create-link" href="/create"
               on:click|preventDefault|stopPropagation={createAccount}>{unknownUser ? I18n.t("login.useExistingAccount") : I18n.t("login.createAccount")}</a>
            <button class:disabled={!allowedNext($user.email, $user.familyName, $user.givenName)} on:click={handleNext}
                    disabled={!allowedNext($user.email, $user.familyName, $user.givenName)}>
                {unknownUser ? I18n.t("login.create") : I18n.t("login.next")}
            </button>
        </div>
    </div>
</div>