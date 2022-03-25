<script>

    import I18n from "i18n-js";
    import {onMount} from "svelte";

    export let id;

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
            setTimeout(() => refs[index + 1].focus(), 15);
        } else {
            //  this.verify();
            setTimeout(() => alert("verify"), 15);
        }
    }

</script>

<style lang="scss">

    p.explanation {
        margin: 15px 0;
        font-size: 14px;
    }

    div.totp-value-container {
        display: flex;
        margin-top: 15px;
        width: 330px;
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

</style>

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