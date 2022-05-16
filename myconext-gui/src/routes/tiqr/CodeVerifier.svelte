<script>
    import critical from "../../icons/critical.svg";

    import I18n from "i18n-js";
    import {onMount, tick} from "svelte";
    import {navigate} from "svelte-routing";
    import {me, sendDeactivationPhoneCode} from "../../api";
    import {user} from "../../stores/user";
    import Spinner from "../../components/Spinner.svelte";

    export let action;
    export let navigateTo;
    export let onValid;
    export let phoneVerificationURL;
    export let reEnter = true;

    let wrongCode = false;
    let maxAttempts = false;
    let showSpinner = false;

    let refs = Array(6).fill("");
    let totp = Array(6).fill("");

    onMount(() => {
        refs[0].focus();
    })

    const sendSMSAgain = () => {
        if (phoneVerificationURL) {
            navigate(phoneVerificationURL);
        } else {
            showSpinner = true;
            sendDeactivationPhoneCode().then(() => {
                totp = Array(6).fill("");
                refs[0].focus();
                wrongCode = false;
                maxAttempts = false;
                showSpinner = false;
            });
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
            tick().then(() => refs[index + 1].focus())
        } else {
            showSpinner = true;
            onValid && onValid();
            action(totp.join(""))
                .then(() => {
                    me().then(json => {
                        for (var key in json) {
                            if (json.hasOwnProperty(key)) {
                                $user[key] = json[key];
                            }
                        }
                        navigate(navigateTo);
                    });
                }).catch(e => {
                totp = Array(6).fill("");
                refs[0].focus();
                wrongCode = true;
                showSpinner = false;
                if (e.status === 429) {
                    maxAttempts = true;
                }
            })
        }
    }


</script>

<style lang="scss">

    div.totp-value-container {
        display: flex;
        margin-top: 15px;
        justify-content: space-between;
        max-width: 400px;

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


    div.error span.svg {
        display: inline-block;
        margin-right: 10px;
    }


</style>
{#if showSpinner}
    <Spinner/>
{/if}

<div class="totp-value-container">

    {#each Array(6).fill("") as val, index}
        <input type="text"
               class="totp-value"
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

{#if wrongCode && !maxAttempts}
    <div class="error">
        <span class="svg">{@html critical}</span>
        <span>{I18n.t("sms.codeIncorrect")}</span>
    </div>
{/if}

{#if maxAttempts}
    <div class="error">
        <span class="svg">{@html critical}</span>
        <div class="max-attempts">
            <span>{I18n.t("sms.maxAttemptsPre")}</span>
            <a href="/phone"
               on:click|preventDefault|stopPropagation={sendSMSAgain}>{I18n.t("sms.here")}</a>
            {#if reEnter}
                <span>{I18n.t("sms.maxAttemptsPost")}</span>
            {:else}
                <span>{I18n.t("sms.maxAttemptsPostNoReEnter")}</span>
            {/if}
        </div>
    </div>
{/if}
