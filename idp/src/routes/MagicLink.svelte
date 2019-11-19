<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";

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
        border-radius: 4px;
        background-color: white;
        height: auto;
        min-height: 500px;
        width: auto;
        min-width: 500px;
        max-width: 500px;
        align-items: center;
    }

    h3 {
        color: #767676
    }

</style>
<div class="home">
    <div class="card">
        <h3>{I18n.t("magicLink.header", {email: $user.email})}</h3>
    </div>
</div>