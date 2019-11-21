<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";

    export let id;

</script>

<style>
    .home {
        display: flex;
        justify-content: center;
        width: 100%;
    }

    .card {
        display: flex;
        flex-direction: column;
        padding: 50px;
        background-color: white;
        height: auto;
        min-height: 500px;
        width: 500px;
    }

    h1 {
        color: #767676;
        margin-bottom: 20px;
    }

    p.info {
        color: #767676;
        margin-bottom: 40px;
    }

    p.mail {
        color: #404040;
        margin-bottom: 60px;
        font-size: 24px;
    }

</style>
<div class="home">
    <div class="card">
        <h1>{@html I18n.t("magicLink.header")}</h1>
        <p class="info">{I18n.t("magicLink.info")}</p>
        <p class="mail">{$user.email}</p>
        <a href="/login/{id}">{I18n.t("magicLink.wrongEmail")}</a>
    </div>
</div>