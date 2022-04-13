<script>
    import I18n from "i18n-js";
    import {pollAuthentication, startTiqrAuthentication} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import pushIcon from "../icons/redesign/undraw_Push_notifications_re_t84m.svg";
    import ImageContainer from "../components/ImageContainer.svelte";
    import {user} from "../stores/user";
    import {poll} from "../utils/poll";
    import {authenticationStatus} from "../constants/authenticationStatus";
    import {proceed} from "../utils/sso";
    import {conf} from "../stores/conf";
    import Cookies from "js-cookie";
    import {cookieNames} from "../constants/cookieNames";
    import {loginPreferences} from "../constants/loginPreferences";

    export let id;

    let showSpinner = true;
    let timeOut = false;
    let serviceName = "";
    let qrCode = "";
    let url = "";
    let showQrCode = false;
    let showTOTPLink = false;
    let status;
    let successResult = null;
    let sessionKey = "";
    let onMobile = "ontouchstart" in document.documentElement;

    onMount(() => {
        startTiqrAuthentication($user.email, id)
            .then(res => {
                showSpinner = false;
                qrCode = res.qr;
                url = res.url;
                sessionKey = res.sessionKey;
                showQrCode = res.tiqrCookiePresent;
                status = authenticationStatus.PENDING;
                poll({
                    fn: () => pollAuthentication(sessionKey, id),
                    validate: res => {
                        const success = res.status === authenticationStatus.SUCCESS;
                        if (success) {
                            successResult = res;
                        }
                        return success;
                    },
                    interval: 1000,
                    maxAttempts: 60 * 15 // 15 minute timeout
                }).then(() => {
                    Cookies.set(cookieNames.LOGIN_PREFERENCE, loginPreferences.APP, {
                        expires: 365,
                        secure: true,
                        sameSite: "Lax"
                    });
                    const redirect = successResult.redirect;
                    const hash = successResult.hash;
                    window.location.href = `${redirect}?h=${hash}`;
                }).catch(() => {
                    timeOut = true;
                });

            })
    });

</script>

<style lang="scss">

    img.qr-code {
        cursor: none;
    }

    p.explanation {
        font-size: 14px;
    }

    .info-row {
        display: flex;
        align-items: center;

        &:not(:last-child) {
            margin-bottom: 15px;
        }

        span.note {
            font-weight: bold;
        }

    }

</style>

{#if showSpinner}
    <Spinner/>
{:else if timeOut}
    <h2 class="header">TODO timeout</h2>
{:else}
    {#if showQrCode}
        <h2 class="header">{I18n.t("useApp.scan")}</h2>
    {:else}
        <h2 class="header">{I18n.t("useApp.header")}</h2>
        <p class="explanation">{I18n.t("useApp.info")}</p>
    {/if}
    <ImageContainer icon={showQrCode ? null : pushIcon} margin={!showQrCode}>
        {#if showQrCode}
            {#if onMobile}
                <a href={url}><img class="qr-code" src="{qrCode}" alt="qr-code"></a>
            {:else}
                <img class="qr-code" src="{qrCode}" alt="qr-code">
            {/if}
        {/if}
    </ImageContainer>

    {#if showQrCode}
        <div class="info-row">
        <span>{I18n.t("useApp.offline")}
            <a href="/qr"
               on:click|preventDefault|stopPropagation={() => showTOTPLink = !showTOTPLink}>{I18n.t("useApp.offlineLink")}</a>
        </span>
        </div>
    {:else}
        <div class="info-row">
        <span>
            <span class="note">{I18n.t("useApp.noNotification")}</span>
            <a href="/qr"
               on:click|preventDefault|stopPropagation={() => showQrCode = !showQrCode}>{I18n.t("useApp.qrCodeLink")}</a>
            <span>{I18n.t("useApp.qrCodePostfix")}</span>
        </span>
        </div>
        <div class="info-row">
        <span>
            <span class="note">{I18n.t("useApp.lost")}</span>
            {@html I18n.t("useApp.lostLink")}
        </span>
        </div>
    {/if}

{/if}


