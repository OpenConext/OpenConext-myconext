<script>
    import critical from "../icons/critical.svg?raw";

    import I18n from "../locale/I18n";
    import {onMount, tick} from "svelte";
    import {links} from "../stores/conf";
    import {textPhoneNumber, validatePhoneCode} from "../api";
    import {navigate} from "svelte-routing";
    import {user} from "../stores/user";
    import Button from "../components/Button.svelte";

    let showSpinner = true;
    let hash = null;
    let wrongCode = false;
    let maxAttempts = false;

    let refs = Array(6).fill("");
    let totp = Array(6).fill("");

    onMount(() => {
        $links.displayBackArrow = false;

        const urlParams = new URLSearchParams(window.location.search);
        hash = urlParams.get("h");
        showSpinner = false;
        refs[0].focus();
    });

    const sendSMSAgain = () => {
        showSpinner = true;
        const trimmedPhoneNumber = $user.phoneNumber.replaceAll(" ", "").replaceAll("-", "");
        textPhoneNumber(hash, trimmedPhoneNumber).then(() => {
            showSpinner = false;
            totp = Array(6).fill("");
            refs[0].focus();
            wrongCode = false;
            maxAttempts = false;
        });
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
            validatePhoneCode(hash, totp.join("")).then(res => {
                navigate(`/congrats?h=${hash}&redirect=${encodeURIComponent(res.redirect)}`);
            }).catch(e => {
                totp = Array(6).fill("");
                refs[0].focus();
                wrongCode = true;
                if (e.status === 429) {
                    maxAttempts = true;
                }
            })

        }
    }

</script>

<style lang="scss">

    p.explanation {
        margin: 15px 0;
    }

    div.totp-value-container {
        display: flex;
        margin: 15px 0 35px 0;
        justify-content: space-between;

        &.with-error {
            margin: 15px 0 0 0;
        }

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
        margin: 25px 0 35px 0;
    }


    div.error span.svg {
        display: inline-block;
        margin-right: 10px;
    }


</style>

<h2 class="header">{I18n.t("Sms.Header.COPY")}</h2>
<p class="explanation">{I18n.t("Sms.Info.COPY")}</p>
<div class="totp-value-container" class:with-error={wrongCode || maxAttempts}>

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
               bind:this={refs[index]}
               spellcheck="false"/>
    {/each}


</div>

{#if wrongCode && !maxAttempts}
    <div class="error">
        <span class="svg">{@html critical}</span>
        <span>{I18n.t("Sms.CodeIncorrect.COPY")}</span>
    </div>
{/if}

{#if maxAttempts}
    <div class="error">
        <span class="svg">{@html critical}</span>
        <div class="max-attempts">
            <span>{I18n.t("Sms.MaxAttemptsPre.COPY")}</span>
            <a href="/phone"
               on:click|preventDefault|stopPropagation={() => navigate(`/phone-verification?h=${hash}`)}>{I18n.t("Sms.Here.COPY")}</a>
            <span>{I18n.t("Sms.MaxAttemptsPost.COPY")}</span>
        </div>
    </div>
{/if}

<div class="options">
    <Button className="cancel" href="/sms" label={I18n.t("Sms.SendSMSAgain.COPY")} onClick={sendSMSAgain}/>
</div>