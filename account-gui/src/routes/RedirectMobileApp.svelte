<script>
    import I18n from "i18n-js";
    import {conf, links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";

    export let action;
    let redirectAppUrl = null;
    let actionTranslateKey = null;

    onMount(() => {
        $links.displayBackArrow = false;
        redirectAppUrl = `${$conf.mobileAppRedirect}/${action}${window.location.search}`;
        const translation = I18n.translations[I18n.locale];
        actionTranslateKey = translation.redirectMobileApp[action] ? action : "fallback";
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
        <h1>{I18n.t(`redirectMobileApp.${actionTranslateKey}.title`)}</h1>
        <p class="info">{@html I18n.t(`redirectMobileApp.${actionTranslateKey}.info`)}</p>
        <p class="hidden">{redirectAppUrl}</p>
        <Button href="/eduid"
                onClick={() => window.location.href = redirectAppUrl}
                label={I18n.t("redirectMobileApp.proceedLink")}/>
    </div>
</div>