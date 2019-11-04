<script>
    import logo from "./logo.svg";
    import {user} from "../stores/user";
    import {validEmail} from "../validation";
    import I18n from "i18n-js";
    import {getUser} from "../api/index";

    export let id;
    let unknownUser = false;
    const next = () => {
        getUser($user.email)
                .then(json => {
                    debugger;
                }).catch(e => {
                    debugger;
        })
    };

    const previous = () => {
        unknownUser = false;
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
    input {
        border: 1px solid #dadce0;
        border-radius: 4px;
        padding: 10px;
        font-size: larger;
        width: 90%;
        margin: 20px 10px;
    }

    h2 {
        color: #767676
    }
</style>
<div class="home">
    <div class="card">
        <div class="container">
            <div class="logo">
                {@html logo}
            </div>
            <h2>{I18n.t("login.header")}</h2>
            <p>{id}</p>
            <input type="email"
                    placeholder={I18n.t("login.emailPlaceholder")}
                    bind:value={$user.email}
                    on:keydown={e => e.key === "Enter" && next()}>
        </div>
        <div class="buttons">
            {#if unknownUser}
                <button on:click={previous}>{I18n.t("login.previous")}</button>
            {/if}}
            <button class:disabled={validEmail($user.email)} on:click={next} disabled={validEmail($user.email)}>{I18n.t("login.next")}</button>
        </div>
    </div>
</div>