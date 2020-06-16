<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";

</script>

<style>
    h1 {
        margin: 16px 0;
        color: var(--color-primary-red);
        font-size: 32px;
        font-weight: bold;

    }

</style>
<div class="home">
    <div class="card">
        <h1>{I18n.t("session.title")}</h1>
        <p>{@html I18n.t("session.info")}</p>
    </div>
</div>