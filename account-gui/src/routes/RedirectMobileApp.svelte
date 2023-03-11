<script>
    import I18n from "i18n-js";
    import {conf, links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";

    export let action;
    let redirectAppUrl = null;

    onMount(() => {
        $links.displayBackArrow = false;

        const urlSearchParams = new URLSearchParams(window.location.search);
        const hash = urlSearchParams.get("h")
        const queryPart = hash ? `?h=${hash}` : "";
        redirectAppUrl = `${$conf.mobileAppRedirect}/${action}${queryPart}`
    });

</script>

<style>
    h1 {
        margin: 16px 0;
        color: var(--color-primary-green);
        font-size: 32px;
        font-weight: bold;
    }

    p.info {
        margin-bottom: 25px;
    }

    p.hidden {
        display: none;
    }

</style>
<div class="home">
    <div class="card">
        <h1>{I18n.t(`redirectMobileApp.${action}.title`)}</h1>
        <p class="info">{@html I18n.t(`redirectMobileApp.${action}.info`)}</p>
        <p class="hidden">{redirectAppUrl}</p>
        <Button href="/eduid"
                onClick={() => window.location.href = redirectAppUrl}
                label={I18n.t("redirectMobileApp.proceedLink")}/>
    </div>
</div>