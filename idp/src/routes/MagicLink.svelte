<script>
    import logo from "./logo.svg";
    import {user} from "../stores/user";
    import I18n from "i18n-js";

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

    h3 {
        color: #767676
    }

</style>
<div class="home">
    <div class="card">
        <div class="container">
            <div class="logo">
                {@html logo}
            </div>
            <h3>{I18n.t("magicLink.header", {email: $user.email})}</h3>
        </div>
    </div>
</div>