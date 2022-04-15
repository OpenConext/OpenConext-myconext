<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import Button from "../components/Button.svelte";
    import Verification from "../components/Verification.svelte";
    import {fetchServiceName} from "../api";
    import Spinner from "../components/Spinner.svelte";
    import {conf, links} from "../stores/conf";

    export let id;
    let explanation = null;
    let serviceName = null;
    let showSpinner = true;

    onMount(() => {
        $links.displayBackArrow = false;

        const urlSearchParams = new URLSearchParams(window.location.search);
        explanation = decodeURIComponent(urlSearchParams.get("explanation"));
        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const proceed = useExternalValidation => {
        const queryParams = ($conf.useExternalValidation && useExternalValidation) ? "?useExternalValidation=true" : ""
        window.location.href = `/myconext/api/idp/oidc/account/${id}${queryParams}`;
    };
</script>

<style>


    h2 {
        margin: 30px 0;
        font-size: 32px;
        color: var(--color-primary-green);
    }

    p.info {
        margin-bottom: 25px;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<div class="home">
    <div class="card">
        <h2>{I18n.t("stepup.header")}</h2>
        <p class="info">{@html I18n.t("stepup.info", {name: serviceName})}</p>
        <Verification explanation={explanation} verified={false}/>
        <Button href="/proceed" onClick={() => proceed(false)}
                className="full"
                label={I18n.t("stepup.link")}/>
        {#if $conf.useExternalValidation}
            <Button href="/proceed" onClick={() => proceed(true)}
                    className="full"
                    label={I18n.t("stepup.linkExternalValidation")}/>
        {/if}

    </div>
</div>