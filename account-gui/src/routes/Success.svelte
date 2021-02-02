<script>
    import I18n from "i18n-js";

</script>

<style>
    h1 {
        margin: 16px 0;
        color: var(--color-primary-green);
        font-size: 32px;
        font-weight: bold;

    }

</style>
<div class="home">
    <div class="card">
        <h1>{I18n.t("success.title")}</h1>
        <p>{@html I18n.t("success.info")}</p>
    </div>
</div>