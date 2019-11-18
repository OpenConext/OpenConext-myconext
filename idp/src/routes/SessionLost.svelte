<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";

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
        box-shadow: 1px 1px 5px 0 rgba(158, 158, 158, 0.6);
        padding: 50px;
        border-radius: 4px;
        background-color: white;
        height: auto;
        min-height: 500px;
        width: auto;
        min-width: 500px;
        max-width: 500px;
        align-items: center;
        color: #767676;
    }
    h2 {
        margin-bottom: 40px;
    }

</style>
<div class="home">
    <div class="card">
        <h2>{I18n.t("session.title")}</h2>
        <p>{@html I18n.t("session.info")}</p>
    </div>
</div>