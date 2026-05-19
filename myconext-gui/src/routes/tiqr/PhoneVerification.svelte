<script>
    import {validPhoneNumber} from "../../validation/regexp";
    import I18n from "../../locale/I18n";
    import critical from "../../icons/critical.svg?raw";
    import Spinner from "../../components/Spinner.svelte";
    import Button from "../../components/Button.svelte";
    import {reTextPhoneNumber, textPhoneNumber} from "../../api";
    import {navigate} from "svelte-routing";
    import Modal from "../../components/Modal.svelte";

    const RECOVERY_CODE = "recovery-code";

    export let change = false;

    let initial = true;
    let phoneNumber = "";
    let showSpinner = false;
    let rateLimited = false;
    let showRateLimitedModal = false;
    let finalizedRegistration = false;
    let recoveryCode = sessionStorage.getItem(RECOVERY_CODE);

    $: allowedNext = validPhoneNumber(phoneNumber);
    $: phoneNumberIncorrect = !initial && !validPhoneNumber(phoneNumber);

    const next = () => {
        initial = false;
        phoneNumberIncorrect = !validPhoneNumber(phoneNumber);
        if (!phoneNumberIncorrect) {
            showSpinner = true;
            const promise = change ? reTextPhoneNumber : textPhoneNumber;
            promise(phoneNumber.replaceAll(" ", "").replaceAll("-", ""))
                .then(() => navigate(`${change ? "change-" : ""}phone-confirmation`))
                .catch(e => {
                    if (e.status === 406) {
                        finalizedRegistration = true;
                    } else {
                        rateLimited = true;
                        showRateLimitedModal = true;
                    }
                    showSpinner = false;
                })
        }
    }

    const init = el => el.focus();

    const handleEnter = e => e.key === "Enter" && next();

</script>

<style lang="scss">
    .phone-verification {
        width: 100%;
        height: 100%;
    }

    .inner-container {
        height: 100%;
        margin: 0 auto;
        padding: 15px 30px 15px 0;
        display: flex;
        flex-direction: column;
    }

    h2 {
        margin: 20px 0 10px 0;
        color: var(--color-primary-green);
    }

    div.error {
        display: flex;
        align-items: center;
        color: var(--color-primary-red);
        margin-bottom: 25px;
    }

    p.explanation {
        margin: 15px 0;
    }

    p.methods {
        margin-bottom: 15px;
    }

    div.error span.svg {
        display: inline-block;
        margin-right: 10px;
    }

    input {
        border: 1px solid #727272;
        border-radius: 6px;
        padding: 14px;
        font-size: 16px;
        width: 100%;
        margin: 8px 0 15px 0;
    }

    input.error {
        border: solid 1px var(--color-primary-red);
        background-color: #fff5f3;
    }

    input.error:focus {
        outline: none;
    }

    span.backup-code {
        font-weight: 600;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<div class="phone-verification">
    <div class="inner-container">
        {#if finalizedRegistration}
            <h2 class="header">{I18n.t("recovery.finalizedRegistration")}</h2>
            <p class="explanation">{I18n.t("recovery.finalizedRegistrationExplanation")}
                <a href="/backup-codes" on:click|preventDefault|stopPropagation={() => navigate("/backup-codes")}>
                    {I18n.t("recovery.finalizedRegistrationHere")}
                </a>
            </p>
            {#if recoveryCode}
                <p class="explanation">{I18n.t("recovery.finalizedRegistrationBackupCode")}
                    <span class="backup-code">{recoveryCode}</span>
                </p>
            {/if}
        {:else}
            <h2 class="header">{I18n.t("PhoneVerification.Header.COPY")}</h2>
            <p class="explanation">{I18n.t("phoneVerification.info")}</p>
            <p class="methods">{I18n.t("PhoneVerification.Text.COPY")}</p>

            <input class:error={phoneNumberIncorrect}
                   type="tel"
                   id="password-field"
                   spellcheck="false"
                   on:keydown={handleEnter}
                   placeholder={I18n.t("PhoneVerification.PlaceHolder.COPY")}
                   use:init
                   bind:value={phoneNumber}>
            {#if phoneNumberIncorrect}
                <div class="error">
                    <span class="svg">{@html critical}</span>
                    <span>{I18n.t("phoneVerification.phoneIncorrect")}</span>
                </div>
            {/if}

            <Button href="/next"
                    larger={true}
                    disabled={showSpinner || !allowedNext}
                    label={I18n.t("PhoneVerification.Verify.COPY")}
                    onClick={next}/>
        {/if}
    </div>
</div>

{#if rateLimited && showRateLimitedModal}
    <Modal submit={() => showRateLimitedModal = false}
           warning={true}
           question={I18n.t("PhoneVerification.RateLimitedInfo.COPY")}
           title={I18n.t("PhoneVerification.RateLimited.COPY")}
           confirmTitle={I18n.t("PhoneVerification.Ok.COPY")}>
    </Modal>
{/if}