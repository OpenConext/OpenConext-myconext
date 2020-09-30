<script>
    import check from "../icons/check.svg";

    export let value;
    export let onChange;
    export let label;
    export let className = "";

</script>
<style>
    label.checkbox {
        display: block;
        position: relative;
        padding-left: 28px;
        cursor: pointer;
        user-select: none;
    }

    label.checkbox.light {
        font-weight: 300;
    }

    .checkbox input {
        position: absolute;
        opacity: 0;
        cursor: pointer;
        height: 0;
        width: 0;
    }

    .checkmark {
        position: absolute;
        top: 0;
        left: 0;
        height: 18px;
        width: 18px;
        border: 1px solid #004c97;
        border-radius: 2px;
    }

    .checkbox:hover input ~ .checkmark {
        background-color: #ebebeb;
    }

    .checkbox input:checked ~ .checkmark {
        background-color: #b3e5ff;
    }

    :global(.checkbox input ~ .checkmark svg) {
        display: none;
    }

    :global(.checkbox input:checked ~ .checkmark svg) {
        display: inherit;
    }

</style>
<label class={`checkbox ${className}`}>
    <span>{@html label}</span>
    <input type="checkbox" checked={value} on:change={e => onChange(e.target.checked)}>
    <span class="checkmark">
        {@html check}
    </span>
</label>
