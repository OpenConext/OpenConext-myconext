<script>
    import {validPhoneNumber} from "../constants/regexp";
    import I18n from "../locale/I18n";
    import critical from "../icons/critical.svg?raw";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import Button from "../components/Button.svelte";
    import {links} from "../stores/conf";
    import {textPhoneNumber} from "../api";
    import {navigate} from "svelte-routing";
    import {user} from "../stores/user";
    import Modal from "../components/Modal.svelte";

    let showSpinner = true;
    let initial = true;
    let hash = null;
    let phoneNumber = "";
    let rateLimited = false;
    let showRateLimitedModal = false;

    onMount(() => {
        $links.displayBackArrow = true;

        const urlParams = new URLSearchParams(window.location.search);
        hash = urlParams.get("h");
        showSpinner = false;
    });

    $: allowedNext = validPhoneNumber(phoneNumber);
    $: phoneNumberIncorrect = !initial && !validPhoneNumber(phoneNumber);

    const next = () => {
        initial = false;
        phoneNumberIncorrect = !validPhoneNumber(phoneNumber);
        if (!phoneNumberIncorrect) {
            showSpinner = true;
            const trimmedPhoneNumber = phoneNumber.replaceAll(" ", "").replaceAll("-", "");
            textPhoneNumber(hash, trimmedPhoneNumber)
                .then(() => {
                    $user.phoneNumber = phoneNumber;
                    navigate(`phone-confirmation?h=${hash}`);
                })
                .catch(() => {
                    rateLimited = true;
                    showRateLimitedModal = true;
                    showSpinner = false;
                })
        }
    }

    const init = el => el.focus();

    const handleEnter = e => e.key === "Enter" && next();

</script>

<style lang="scss">

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
<h2 class="header">{I18n.t("PhoneVerification.Header.COPY")}</h2>
<p class="explanation">{I18n.t("phoneVerification.info")}</p>
<p class="methods">{I18n.t("PhoneVerification.Text.COPY")}</p>

<input class:error={phoneNumberIncorrect}
       autocomplete="current-password"
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
        disabled={showSpinner || !allowedNext || rateLimited}
        label={I18n.t("PhoneVerification.Verify.COPY")}
        className="full"
        onClick={next}/>

{#if rateLimited && showRateLimitedModal}
    <Modal submit={() => showRateLimitedModal = false}
           warning={true}
           question={I18n.t("phoneVerification.rateLimitedInfo")}
           title={I18n.t("phoneVerification.rateLimited")}
           confirmLabel={I18n.t("phoneVerification.ok")}>
    </Modal>
{/if}
