<script>
    import I18n from "../locale/I18n";
    import Button from "../components/Button.svelte";

</script>

<style>
    .not-found {
        display: flex;
        flex-direction: column;
    }

    h1 {
        color:var(--color-primary-red);
        font-size: 32px;
        font-weight: 600;
        margin: 16px 0;
    }

    p {
        margin-bottom: 40px;
        font-size: 18px;
    }

</style>
<div class="not-found">
    <h1>{I18n.t("NotFound.Title.COPY")}</h1>
    <p>{I18n.t("NotFound.Title2.COPY")}</p>
    <Button label={I18n.t("Expired.Back.COPY")} href="https://www.eduid.nl" onClick={() => window.location.href = "https://www.eduid.nl"} />
</div>