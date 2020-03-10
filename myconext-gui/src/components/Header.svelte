<script>

    import I18n from "i18n-js";
    import eduidLogo from "../img/logo_eduID_2020.svg";
    import {navigate} from "svelte-routing";
    import {logout} from "../api";
    import {user} from "../stores/user";
    import Button from "./Button.svelte";

    const logoutUser = () => {
        logout().then(() => {
            $user = {
                id: "",
                email: "",
                givenName: "",
                familyName: "",
                guest: true,
                usePassword: false
            };
            navigate("/landing?logout=true");
        });
    }

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

    div.logout {
        margin: 0 25px 0 auto;
    }

</style>
<div class="header">

    <div class="logo">
        <a href="/">
            {@html eduidLogo}
        </a>
    </div>
    <h1>{I18n.ts("header.title")}</h1>
    {#if !$user.guest}
        <div class="logout">
            <Button href="/logout" label={I18n.t("header.logout")} onClick={logoutUser} className="cancel small"/>
        </div>
    {/if}
</div>
