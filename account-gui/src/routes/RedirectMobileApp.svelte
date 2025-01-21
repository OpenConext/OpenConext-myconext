<script>
    import I18n from "i18n-js";
    import {conf, links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import QrCode from "svelte-qrcode"

    export let action;
    let redirectAppUrl = null;
    let actionTranslateKey = null;
    let isMobile = "ontouchstart" in window || navigator.maxTouchPoints > 0;

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
        font-weight: 600;
    }

    p.info {
        margin-bottom: 25px;
    }

    p.hidden {
        display: none;
    }

    .qr-container {
        display: flex;
        flex-direction: column;
        :global(img) {
            margin: 25px auto 0 auto;
        }
    }

</style>
<div class="home">
    <div class="card">
        <h1>{I18n.t(`redirectMobileApp.${actionTranslateKey}.title`)}</h1>
        <p class="info">{@html I18n.t(`redirectMobileApp.${actionTranslateKey}.${isMobile ? "info" : "infoDesktop"}`)}</p>
        <p class="hidden">{redirectAppUrl}</p>
        {#if isMobile}
            <Button href="/eduid"
                    onClick={() => window.location.href = redirectAppUrl}
                    label={I18n.t("redirectMobileApp.proceedLink")}/>
        {:else}
            <div class="qr-container">
                <p>{I18n.t("redirectMobileApp.qrCodeLink")}</p>
                <QrCode value={redirectAppUrl}/>
            </div>
        {/if}
    </div>
</div>