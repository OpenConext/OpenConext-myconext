<script>
    export let value;
    export let onChange;
    export let label;
</script>
<style>
    label.checkbox {
        display: block;
        margin-top: 10px;
        position: relative;
        padding-left: 28px;
        cursor: pointer;
        user-select: none;
    }

    /* Hide the browser's default checkbox */
    .checkbox input {
        position: absolute;
        opacity: 0;
        cursor: pointer;
        height: 0;
        width: 0;
    }

    /* Create a custom checkbox */
    .checkmark {
        position: absolute;
        top: 0;
        left: 0;
        height: 18px;
        width: 18px;
        border: 1px solid #004c97;
        border-radius: 2px;
        /*box-shadow: 0 1px 1px 0 rgba(141, 141, 141, 0.75);*/
    }

    /* On mouse-over, add a grey background color */
    .checkbox:hover input ~ .checkmark {
        background-color: #fafafa;
    }

    /* When the checkbox is checked, add a blue background */
    .checkbox input:checked ~ .checkmark {
        background-color: #b3e5ff;
    }

    /* Create the checkmark/indicator (hidden when not checked) */
    .checkmark:after {
        content: "";
        position: absolute;
        display: none;
    }

    /* Show the checkmark when checked */
    .checkbox input:checked ~ .checkmark:after {
        display: block;
    }

</style>
<label class="checkbox">
    <span>{label}</span>
    <input type="checkbox" checked={value} on:change={e => onChange(e.target.checked)}>
    <span class="checkmark"></span>
</label>
