<script>

    import I18n from "i18n-js";
    import eduidLogo from "../img/logo_eduID.svg";
    import Button from "./Button.svelte";
    import {config} from "../stores/config";
    import {navigate} from "svelte-routing";

    const login = () => window.location.href = `${$config.eduIDDoLoginUrl}?lang=${I18n.locale}`;

</script>

<style>

    .header {
        width: 100%;
        max-width: var(--width-app);
        margin: 0 auto;
        display: flex;
        background-color: var(--color-primary-blue);
        align-items: center;
        align-content: center;
        position: relative;
        flex-direction: row;
        height: 80px;
        color: #94d6ff;
        border-left: 28px solid var(--color-primary-blue);

    }

    .logo {
        padding: 10px 0;
    }

    h1 {
        display: none;
    }

    @media (max-width: 800px) {
        h1 {
            display: none;
        }
    }

    div.mine {
        margin: 0 25px 0 auto;
    }

</style>
<div class="header">

    <div class="logo">
        <a href="/" on:click|preventDefault|stopPropagation={() => navigate("/")}>
            {@html eduidLogo}
        </a>
    </div>
    <h1>{I18n.t("header.title")}</h1>
    <div class="mine">
        <Button href="/eduID" label={I18n.t("header.mine")} onClick={login} className="cancel small"/>
    </div>
</div>
