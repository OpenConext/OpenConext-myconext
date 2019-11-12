<script>
    import logo from "./logo.svg";
    import {user} from "../stores/user";
    import {validEmail} from "../validation/regexp";
    import I18n from "i18n-js";
    import {magicLinkNewUser, magicLinkExistingUser} from "../api/index";
    import { navigate } from "svelte-routing";

    export let id;
    let unknownUser = false;
    const handleNext = () => magicLinkExistingUser($user.email,$user.rememberMe, id)
            .then(json => {
                navigate("/magic", {replace:  true});
            }).catch(e => {
                // debugger;
                unknownUser = true;
            });


    const createAccount = () => {
        unknownUser = true;
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
        display: inline-block;
        margin: auto 15px 15px auto;
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

    button.disabled {
        cursor: not-allowed;
        color: #C5C5C5;
        background-color: whitesmoke;
    }

    input[type=email], input[type=text] {
        border: 1px solid #dadce0;
        border-radius: 4px;
        padding: 10px;
        font-size: larger;
        width: 100%;
        margin: 20px 10px;
    }

    h2, label {
        color: #767676
    }

    div.checkbox {
        display: flex;
        margin-right: auto;
        align-items: center;
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
            {#if unknownUser}
                <input type="text"
                       placeholder={I18n.t("login.giveNamePlaceholder")}
                       bind:value={$user.givenName}>
                <input type="text"
                       placeholder={I18n.t("login.familyNamePlaceholder")}
                       bind:value={$user.familyName}>
            {/if}
            <div class="checkbox">
                <input type="checkbox"
                       id="remember-me"
                       bind:checked={$user.rememberMe}/>
                <label for="remember-me">{I18n.t("login.rememberMe")}</label>
            </div>
        </div>
        <div class="buttons">
            <a href="/create" on:click={createAccount}>{I18n.t("login.createAccount")}</a>
            <button class:disabled={!validEmail($user.email)} on:click={handleNext}
                    disabled={!validEmail($user.email)}>{unknownUser ? I18n.t("login.create") : I18n.t("login.next")}</button>
        </div>
    </div>
</div>