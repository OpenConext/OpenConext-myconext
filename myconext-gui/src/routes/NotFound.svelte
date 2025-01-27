<script>
    import I18n from "../locale/I18n";

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
        font-weight: 600;
        margin: 40px 0;
    }

    p {
        margin-bottom: 40px;
    }

</style>
<div class="not-found">
    <h1>{I18n.t("NotFound.Title.COPY")}</h1>
    <p>{I18n.t("NotFound.Title2.COPY")}</p>
</div>