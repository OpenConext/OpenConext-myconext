<script>
    import I18n from "../locale/I18n";
    import Button from "../components/Button.svelte";
    import {config} from "../stores/user";


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
<h1>{I18n.t("Expired.Title.COPY")}</h1>
<p>{@html I18n.t("Expired.Info.COPY")}</p>
<Button label={I18n.t("Expired.Back.COPY")} href={`/create-from-institution`}
        onClick={() => window.location.href = `/create-from-institution`}/>
