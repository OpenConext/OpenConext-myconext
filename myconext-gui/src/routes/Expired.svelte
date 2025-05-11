<script>
    import I18n from "../locale/I18n";
    import Button from "../components/Button.svelte";
</script>

<style>
    div.expired {
        display: flex;
        flex-direction: column;
        padding: 25px;
    }

    h1 {
        margin: 16px 0;
        color: var(--color-primary-red);
        font-size: 32px;
        font-weight: 600;

    }

    p {
        color: var(--color-primary-black);
        margin-bottom: 40px;
    }

</style>
<div class="expired">
    <h1>{I18n.t("MaxAttempt.Title.COPY")}</h1>
    <p>{@html I18n.t("MaxAttempt.Info.COPY")}</p>
    <Button label={I18n.t("Expired.Back.COPY")}
            href={`/create-from-institution`}
            onClick={() => window.location.href = `/create-from-institution`}/>
</div>