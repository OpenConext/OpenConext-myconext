<script>
    import I18n from "../locale/I18n";
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
<h1>{I18n.t("Expired.CodeTitle.COPY")}</h1>
<p>{@html I18n.t("Expired.CodeInfo.COPY")}</p>
<Button label={I18n.t("Expired.Back.COPY")} href={`https://${$conf.domain}`}
        onClick={() => window.location.href = `https://${$conf.domain}`}/>
