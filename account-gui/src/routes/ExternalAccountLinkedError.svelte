<script>
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import DOMPurify from "dompurify";
    import {conf, links} from "../stores/conf";

    $links.displayBackArrow = false;
    let errorDescription = null;

    onMount(() => {
        const urlParams = new URLSearchParams(window.location.search);
        errorDescription = decodeURIComponent(urlParams.get("error_description"));
    });

</script>

<style lang="scss">
    h1 {
        margin: 16px 0;
        color: var(--color-primary-red);
        font-size: 32px;
        font-weight: bold;

    }

    p {
        margin-bottom: 12px;
        color: var(--color-primary-black);

        &.last {
            margin-bottom: 40px;
            font-style: italic;
        }
    }

</style>
<h1>{I18n.t("externalAccountLinkedError.header")}</h1>
<p>{@html I18n.t("externalAccountLinkedError.info")}</p>
{#if errorDescription}
    <p class="last">{@html DOMPurify.sanitize(errorDescription)}</p>
{/if}

<Button label={I18n.t("expired.back")} href={`https://${$conf.domain}`}
        onClick={() => window.location.href = `https://${$conf.domain}`}/>
