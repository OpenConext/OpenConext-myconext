<script>
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import DOMPurify from "dompurify";

    let errorDescription = null;
    let busy = false;

    onMount(() => {
        const urlParams = new URLSearchParams(window.location.search);
        errorDescription = decodeURIComponent(urlParams.get("error_description"));
    });

    const retry = () => {
        busy = true;
        navigate("/personal?retry=true")
    };

</script>

<style lang="scss">
    .external-account-linked-error {
        display: flex;
        flex-direction: column;
        padding: 25px;

        h1 {
            color: var(--color-primary-red);
            font-size: 32px;
            font-weight: bold;
            margin: 22px 0;
        }

        p {
            margin-bottom: 12px;

            &.last {
                margin-bottom: 40px;
                font-style: italic;
            }
        }
    }


</style>
<div class="external-account-linked-error">
    <h1>{I18n.t("externalAccountLinkedError.header")}</h1>
    <p>{I18n.t("externalAccountLinkedError.info")}</p>
    {#if errorDescription}
        <p class="last">{@html DOMPurify.sanitize(errorDescription)}</p>
    {/if}
    <Button href={`/link`}
            didisabled={busy}
            large={true}
            label={I18n.t("externalAccountLinkedError.retryLink")}
            onClick={retry}/>
</div>