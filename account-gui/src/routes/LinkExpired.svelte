<script>
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";
    import {conf} from "../stores/conf";
</script>

<style>
    h1 {
        margin: 16px 0;
        color: var(--color-primary-red);
        font-size: 32px;
        font-weight: bold;

    }

    p {
        color: var(--color-primary-black);
        margin-bottom: 40px;
    }

</style>
<div class="home">
    <div class="card">
        <h1>{I18n.t("expired.title")}</h1>
        <p>{@html I18n.t("expired.info")}</p>
        <Button label={I18n.t("expired.back")} href={`https://${$conf.domain}`}
                onClick={() => window.location.href = `https://${$conf.domain}`}/>
    </div>
</div>