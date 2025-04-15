<script>
    import {onMount} from "svelte";
    import {isEmpty, stopEvent} from "../utils/utils";

    export let size;
    export let verify;
    export let intermediateCallback;
    export let disabled = false;
    export let validate;
    export let transformer;
    export let focusFirst = true;
    export let info;

    const timeout = 35;
    const delKeys = ["Delete", "Backspace"];

    let values = Array(size).fill("");
    let inputRefs = new Map();

    onMount(() => {
        values.forEach((_, index) => inputRefs.set(index, document.querySelector(`#ref_${index}`)));
        if (focusFirst && !disabled) {
            const firstElement = inputRefs.get(0);
            setTimeout(() => firstElement.focus(), 165);
        }
    });

    const handleChange = (index, e) => {
        const val = e.target.value;

        if (validate && !validate(val)) {
            e.target.value = "";
            stopEvent(e);
            return;
        }

        values[index] = transformer ? transformer(val) : val;
        values = [...values]; // trigger reactivity

        if (intermediateCallback) {
            intermediateCallback(values);
        }

        if (index !== size - 1 && !isEmpty(val)) {
            setTimeout(() => inputRefs.get(index + 1)?.focus(), timeout);
        } else if (!isEmpty(val)) {
            setTimeout(() => verify(values.join("")), timeout);
        }
    }

    const handleKeyDown = (index, e) => {
        if (delKeys.includes(e.key) && index > 0 && e.target.value === "") {
            inputRefs.get(index - 1)?.focus();
        }
    }

    const handlePaste = (index, e) => {
        if (index !== 0) {
            return;
        }

        const data = e.clipboardData?.getData("text/plain") || "";
        const newValues = data.split("");

        if (newValues.length !== size) {
            return;
        }
        if (validate && newValues.some(val => !validate(val))) {
            return;
        }

        values = transformer ? newValues.map(val => transformer(val)) : newValues;
        inputRefs.get(inputRefs.size - 1)?.focus();

        setTimeout(() => verify(values.join("")), timeout);
    }
</script>

<style lang="scss">
    .sds--code-validation-container {
        .sds--code-validation {
            display: flex;
            gap: 22px;

            input.value {
                width: 40px;
                padding-left: 12px;
                height: 40px;
                font-size: 22px;
                border: 1px solid #8C969F;

                &[disabled] {
                    background-color: #F4F6F8;
                    border: 1px solid #B2B6BE;
                }
            }
        }

        p.info {
            margin-top: 15px;
        }
    }
</style>

<div class="sds--code-validation-container">
    <div class="sds--code-validation">
        {#each Array(size) as _, index}
            <input id={`ref_${index}`}
                   type="text"
                   class={`value index_${index}`}
                   maxLength="1"
                   value={values[index] || ""}
                   disabled={ disabled || (isEmpty(values[index]) && index !== 0 && isEmpty(values[index - 1]))}
                   on:input={e => handleChange(index, e)}
                   on:keydown={e => handleKeyDown(index, e)}
                   on:paste={e => handlePaste(index, e)}
            />
        {/each}
    </div>
    {#if info}
        <p class="info">{@html info}></p>
    {/if}
</div>
