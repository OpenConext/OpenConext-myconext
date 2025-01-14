<script>
    import I18n from "../locale/I18n";

    import {manualResponse, pollAuthentication, startTiqrAuthentication} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {onDestroy, onMount, tick} from "svelte";
    import pushIcon from "../icons/redesign/undraw_Push_notifications_re_t84m.svg?raw";
    import ImageContainer from "../components/ImageContainer.svelte";
    import {user} from "../stores/user";
    import {poll, suspensionMinutes} from "../utils/poll";
    import {authenticationStatus} from "../constants/authenticationStatus";
    import critical from "../icons/critical.svg?raw";
    import {links} from "../stores/conf";
    import Cookies from "js-cookie";
    import {cookieNames} from "../constants/cookieNames";
    import {loginPreferences} from "../constants/loginPreferences";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";

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
    let suspendedResult = null;
    let sessionKey = "";
    let onMobile = "ontouchstart" in document.documentElement;

    let refs = Array(6).fill("");
    let totp = Array(6).fill("");
    let wrongResponse = false;

    $: minutes = suspendedResult ? suspensionMinutes(suspendedResult["suspendedUntil"]) : null;

    const toggleShowTOTPLink = () => {
        showTOTPLink = !showTOTPLink;
        if (showTOTPLink) {
            tick().then(() => refs[0].focus().focus());
        }
    }

    const onKeyDownTotp = index => e => {
        if ((e.key === "Delete" || e.key === "Backspace") && index > 0 && e.target.value === "") {
            refs[index - 1].focus();
        }
        return true;
    }

    const onInputTotp = index => e => {
        const val = e.target.value;
        if (isNaN(val)) {
            e.target.value = "";
            return;
        }
        const newValue = [...totp];
        newValue[index] = val;
        totp = newValue;
        if (index !== 5) {
            tick().then(() => refs[index + 1].focus());
        } else {
            wrongResponse = false;
            showSpinner = true;
            manualResponse(sessionKey, totp.join(""))
                .then(() => {
                    //Do nothing the next poll will be successful
                    wrongResponse = false;
                }).catch(() => {
                totp = Array(6).fill("");
                showSpinner = false;
                wrongResponse = true;
                tick().then(() => refs[0].focus().focus());

            })

        }
    }

    onDestroy(() => timeOut = true);

    onMount(() => {
        $links.displayBackArrow = true;
        startTiqrAuthentication($user.email, id)
            .then(res => {
                showSpinner = false;
                qrCode = res.qr;
                url = res.url;
                sessionKey = res.sessionKey;
                showQrCode = !res.tiqrCookiePresent;
                status = authenticationStatus.PENDING;
                poll({
                    fn: () => pollAuthentication(sessionKey, id),
                    validate: res => {
                        const success = res.status === authenticationStatus.SUCCESS;
                        if (success) {
                            successResult = res;
                        }
                        suspendedResult = res.status === authenticationStatus.SUSPENDED ? res : null;
                        return success || timeOut;
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
            }).catch(() => navigate(`/options/${id}`))
    });

</script>

<style lang="scss">

    .mobile-qr-code {
        display: flex;
        flex-direction: column;

        .button-link-container {
            margin: auto;
        }
    }

    img.qr-code {
        cursor: none;
    }

    .on-mobile {
        margin-top: 15px;
    }

    .info-row {
        display: flex;
        align-items: center;

        &:not(:last-child) {
            margin-bottom: 15px;
        }

        span.note {
            font-weight: 600;
        }

    }

    p.time-out {
        margin-top: 20px;
    }

    div.totp-value-container {
        display: flex;
        margin-top: 15px;
        justify-content: space-between;

        input.totp-value {
            width: 40px;
            padding-left: 14px;
            height: 40px;
            font-size: 22px;
            border: 1px solid var(--color-primary-blue);
            border-radius: 2px;

            &[disabled] {
                background-color: rgba(239, 239, 239, 0.3);
                border: 1px solid rgba(118, 118, 118, 0.3);
            }
        }
    }

    div.error {
        display: flex;
        align-items: center;
        color: var(--color-primary-red);
        margin: 25px 0;

    }

    div.suspended {
        display: flex;
        flex-direction: column;
    }


    div.error span.svg {
        display: inline-block;
        margin-right: 10px;
    }


</style>

{#if showSpinner}
    <Spinner/>
{:else if timeOut}
    <h2 class="header">{I18n.t("UseApp.TimeOut.COPY")}</h2>
    <p class="time-out">
        <span>{I18n.t("UseApp.TimeOutInfoFirst.COPY")}</span>
        <a href="/"
           on:click|preventDefault|stopPropagation={() => window.location.reload(true)}>{I18n.t("UseApp.TimeOutInfoLink.COPY")}</a>
        <span>{I18n.t("UseApp.TimeOutInfoLast.COPY")}</span>
    </p>
{:else}
    {#if showQrCode}
        <h2 class="header">{I18n.t("UseApp.Scan.COPY")}</h2>
    {:else}
        <h2 class="header">{I18n.t("UseApp.Header.COPY")}</h2>
        <p class="explanation">{I18n.t("UseApp.Info.COPY")}</p>
    {/if}
    <ImageContainer icon={showQrCode ? null : pushIcon} margin={!showQrCode}>
        {#if showQrCode}
            {#if onMobile}
                <div class="mobile-qr-code">
                    <a class="qr-code-link" href={url}>
                        <img class="qr-code" src="{qrCode}" alt="qr-code">
                    </a>
                    <div class="button-link-container">
                        <Button href={url}
                                onClick={() => window.location.href = url}
                                label={I18n.t("UseApp.OpenEduIDApp.COPY")}/>
                    </div>
                </div>
            {:else}
                <img class="qr-code" src="{qrCode}" alt="qr-code">
            {/if}
        {/if}
    </ImageContainer>

    {#if showQrCode }
        <div class="info-row">
            <span class:on-mobile={onMobile}>{I18n.t("UseApp.Offline.COPY")}
                <a href="/qr"
                   on:click|preventDefault|stopPropagation={toggleShowTOTPLink}>{I18n.t("UseApp.OfflineLink.COPY")}</a>
            </span>
        </div>
        {#if showTOTPLink}
            <div class="totp-value-container">

                {#each Array(6).fill("") as val, index}
                    <input type="text"
                           class="totp-value"
                           spellcheck="false"
                           id={`number-${index}`}
                           name={`number-${index}`}
                           disabled={(totp[index] || "").length === 0 && ((index !== 0 && totp[index - 1] === ""))}
                           maxlength={1}
                           value={totp[index] || ""}
                           on:input={onInputTotp(index)}
                           on:keydown={onKeyDownTotp(index)}
                           bind:this={refs[index]}/>
                {/each}
            </div>
            {#if wrongResponse}
                <div class="error">
                    <span class="svg">{@html critical}</span>
                    <span>{I18n.t("UseApp.ResponseIncorrect.COPY")}</span>
                </div>
            {/if}
        {/if}
    {:else}
        <div class="info-row">
        <span>
            <span class="note">{I18n.t("UseApp.NoNotification.COPY")}</span>
            <a href="/qr"
               on:click|preventDefault|stopPropagation={() => showQrCode = !showQrCode}>{I18n.t("UseApp.QrCodeLink.COPY")}</a>
            <span>{I18n.t("UseApp.QrCodePostfix.COPY")}</span>
        </span>
        </div>
        <div class="info-row">
        <span>
            <span class="note">{I18n.t("UseApp.Lost.COPY")}</span>
            {@html I18n.t("UseApp.LostLink.COPY")}
        </span>
        </div>
    {/if}
    {#if suspendedResult}
        <div class="error">
            <span class="svg">{@html critical}</span>
            <div class="suspended">
                <span>{I18n.t("UseApp.SuspendedResult.COPY")}</span>
                {#if minutes > 0}
                    <span>{I18n.t("UseApp.AccountSuspended.COPY",
                        {
                            minutes: minutes,
                            plural: I18n.t(`useApp.${minutes === 1 ? "minute" : "minutes"}`)
                        }
                    )}
                    </span>
                {:else}
                    <span>{I18n.t("UseApp.AccountNotSuspended.COPY")}</span>
                {/if}
            </div>
        </div>
    {/if}
{/if}


