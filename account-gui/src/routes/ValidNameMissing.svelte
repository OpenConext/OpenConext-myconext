<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {conf} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {fetchServiceName} from "../api";
    import {proceed} from "../utils/sso";

    export let id;
    let serviceName = null;
    let showSpinner = true;

    onMount(() => {
        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const retry = () => {
        window.location.href = `/myconext/api/idp/oidc/account/${id}?forceAuth=true`;
    };

</script>

<style>


    h2 {
        margin: 30px 0 40px 0;
        font-size: 32px;
        color: var(--color-primary-green);
    }

    p.info {
        margin-bottom: 25px;
    }

    div.last {
        margin-top: 25px;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<div class="home">
    <div class="card">
        <h2>{I18n.t("validNameMissing.header")}</h2>
        <p class="info">{I18n.t("validNameMissing.info")}</p>
        <p class="info">{I18n.t("validNameMissing.proceed", {name: serviceName})}</p>

        <Button href="/proceed" onClick={() => proceed($conf.magicLinkUrl)}
                className="cancel"
                label={I18n.t("validNameMissing.proceedLink")}/>
        <div class="last">
            <Button href="/retry" onClick={retry}
                    label={I18n.t("validNameMissing.retryLink")}/>
        </div>

    </div>
</div>