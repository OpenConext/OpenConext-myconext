<script>
    import I18n from "i18n-js";
    import {fetchQrCode} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import pushIcon from "../icons/redesign/undraw_Push_notifications_re_t84m.svg";

    export let id;
    let showSpinner = true;
    let serviceName = "";
    let qrCode = "";
    let url = "";//http://localhost:3000/useapp/cbd3ac9c-6fed-475c-bb6a-f15b69692908?url=https%3A%2F%2F27c6fc4d-459f-4b84-919c-31bde709490a%40eduid.nl%2Ftiqrauth%2F565d54f5f28419adf59d70eda191ade57965d8b68319813dfe33bf9c6787b2c4%2F3f69148ab4%2F14ff7abca25ff92859f7c4623438465ffee46f1ac4f292f5a5892de1cabae16bb8c6539012b84f0f5e4005ca4771a352e0ebd7e9cda147aee801c74e3425cc11%2F1
    let showQrCode = false;
    let showTOTPLink = false;
    let onMobile = "ontouchstart" in document.documentElement;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        url = urlSearchParams.get("url")
        fetchQrCode(url).then(res => {
            qrCode = res.qrcode;
            showSpinner = false;
        });
    });

</script>

<style lang="scss">

    p.explanation {
        font-size: 14px;
    }

    .icon-container {
        display: flex;
        margin: 25px 0;

        img {
            width: 260px;
            height: auto;
            margin: auto;
        }
    }

    :global(.icon-container svg) {
        width: 260px;
        height: auto;
        margin: auto;
    }

    .info-row {
        display: flex;
        align-items: center;

        span.note {
            font-weight: bold;
        }

    }

</style>

{#if showSpinner}
    <Spinner/>
{/if}
{#if showQrCode}
    <h2 class="header">{I18n.t("useApp.scan")}</h2>
{:else}
    <h2 class="header">{I18n.t("useApp.header")}</h2>
    <p class="explanation">{I18n.t("useApp.info")}</p>
{/if}
<div class="icon-container">
    {#if showQrCode}
        {#if onMobile}
            <a href={url}><img src="{qrCode}" alt="qr-code"></a>
        {:else}
            <img src="{qrCode}" alt="qr-code">
        {/if}
    {:else}
        {@html pushIcon}
    {/if}
</div>

<div class="info-row">
    {#if showQrCode}
        <span>{I18n.t("useApp.offline")}
            <a href="/qr"
               on:click|preventDefault|stopPropagation={() => showTOTPLink = !showTOTPLink}>{I18n.t("useApp.offlineLink")}</a>
        </span>

    {:else}
        <span>
            <span class="note">{I18n.t("useApp.noNotification")}</span>
            <a href="/qr"
               on:click|preventDefault|stopPropagation={() => showQrCode = !showQrCode}>{I18n.t("useApp.qrCodeLink")}</a>
            <span>{I18n.t("useApp.qrCodePostfix")}</span>
        </span>


    {/if}
</div>


