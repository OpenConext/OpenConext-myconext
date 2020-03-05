<script>
    import I18n from "i18n-js";
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
        font-weight: bold;
        margin: 40px 0;
    }

    p {
        margin-bottom: 40px;
        font-size: 18px;
    }

</style>
<div class="not-found">
    <h1>{I18n.ts("notFound.title")}</h1>
    <p>{I18n.ts("notFound.title2")}</p>
    <Button label={I18n.t("expired.back")} href="https://www.eduid.nl" onClick={() => window.location.href = "https://www.eduid.nl"} />
</div>