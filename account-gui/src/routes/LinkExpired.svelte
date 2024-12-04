<script>
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";
    import {conf, links} from "../stores/conf";

    $links.displayBackArrow = false;

</script>

<style>
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
<h1>{I18n.t("expired.title")}</h1>
<p>{@html I18n.t("expired.info")}</p>
<Button label={I18n.t("expired.back")} href={`https://${$conf.domain}`}
        onClick={() => window.location.href = `https://${$conf.domain}`}/>
