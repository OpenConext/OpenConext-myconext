<script>
    import critical from "../../icons/critical.svg";

    import I18n from "i18n-js";
    import {onMount, tick} from "svelte";
    import {validatePhoneCode} from "../../api";
    import {navigate} from "svelte-routing";

    let wrongCode = false;

    let refs = Array(6).fill("");
    let totp = Array(6).fill("");

    onMount(() => {
        refs[0].focus();
    })

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
            validatePhoneCode(totp.join(""))
                .then(res => {
                    navigate(`/congrats`);
                }).catch(() => {
                totp = Array(6).fill("");
                refs[0].focus();
                wrongCode = true;
            })

        }
    }

</script>

<style lang="scss">

    .phone-confirmation {
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

    p.explanation {
        margin: 15px 0;
    }

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
<div class="phone-confirmation">
    <div class="inner-container">

        <h2 class="header">{I18n.t("sms.header")}</h2>
        <p class="explanation">{I18n.t("sms.info")}</p>
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

        {#if wrongCode}
            <div class="error">
                <span class="svg">{@html critical}</span>
                <span>{I18n.t("sms.codeIncorrect")}</span>
            </div>
        {/if}
    </div>
</div>