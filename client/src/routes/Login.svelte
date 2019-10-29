<script>
    import logo from "./logo.svg";
    import InputElement from "../components/Input.svelte";
    import ButtonElement from "../components/Button.svelte";
    import {user} from "../stores/user";
    import I18n from "i18n-js";</script>

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
</style>
<div class="home">
    <div class="card">
        <div class="container">
            <div class="logo">
                {@html logo}
            </div>
            <h2>{I18n.t("login.header")}</h2>
            <InputElement placeholder={I18n.t("login.emailPlaceholder")}
                          onChange={e => $user.email = e.target.value})}
                          value={$user.email}/>
        </div>
        <div class="buttons">
            <ButtonElement text={I18n.t("login.next")}/>
        </div>
    </div>
</div>