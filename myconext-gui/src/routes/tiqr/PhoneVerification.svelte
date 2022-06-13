<script>
    import {validPhoneNumber} from "../../validation/regexp";
    import I18n from "i18n-js";
    import critical from "../../icons/critical.svg";
    import Spinner from "../../components/Spinner.svelte";
    import Button from "../../components/Button.svelte";
    import {textPhoneNumber} from "../../api";
    import {navigate} from "svelte-routing";

    let initial = true;
    let phoneNumber = "";
    let showSpinner = false;

    $: allowedNext = validPhoneNumber(phoneNumber);
    $: phoneNumberIncorrect = !initial && !validPhoneNumber(phoneNumber);

    const next = () => {
        initial = false;
        phoneNumberIncorrect = !validPhoneNumber(phoneNumber);
        if (!phoneNumberIncorrect) {
            showSpinner = true;
            textPhoneNumber(phoneNumber.replaceAll(" ", "").replaceAll("-", ""))
                .then(() => navigate(`phone-confirmation`));
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


</style>
{#if showSpinner}
    <Spinner/>
{/if}
<div class="phone-verification">
    <div class="inner-container">

        <h2 class="header">{I18n.t("phoneVerification.header")}</h2>
        <p class="explanation">{I18n.t("phoneVerification.info")}</p>
        <p class="methods">{I18n.t("phoneVerification.text")}</p>

        <input class:error={phoneNumberIncorrect}
               autocomplete="current-password"
               id="password-field"
               on:keydown={handleEnter}
               placeholder={I18n.t("phoneVerification.placeHolder")}
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
                label={I18n.t("phoneVerification.verify")}
                onClick={next}/>

    </div>
</div>