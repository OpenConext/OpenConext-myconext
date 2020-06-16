<script>
    import I18n from "i18n-js";

</script>

<style>
    .not-found {
        display: flex;
        flex-direction: column;
        padding: 25px;
    }

    h1 {
        color:var(--color-primary-red);
        font-size: 32px;
        font-weight: bold;
        margin: 40px 0;
    }

    p {
        margin-bottom: 40px;
        font-size: 18px;
    }

</style>
<div class="not-found">
    <h1>{I18n.t("notFound.title")}</h1>
    <p>{I18n.t("notFound.title2")}</p>
</div>