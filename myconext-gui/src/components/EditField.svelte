<script>
    import editIcon from "../icons/edit.svg";
    import Button from "./Button.svelte";
    import I18n from "i18n-js";
    import critical from "../icons/critical.svg";

    export let initialValue;
    export let linkedAccount;
    export let onSave;
    export let label;
    export let editableByUser = true;
    export let readOnly = false;
    export let error = false;
    export let errorMessage;
    export let saveLabel = I18n.t("edit.save");

    let editMode = false;
    let value = initialValue;

    const cancel = () => {
        editMode = false;
        value = initialValue;
    }

</script>
<style lang="scss">

    .edit-field {
        margin-bottom: 20px;
    }

    label {
        font-weight: 600;
        margin-bottom: 10px;
        display: inline-block;
    }

    .edit-mode {
        display: flex;
        gap: 25px;

        input {
            flex-grow: 2;
            border-radius: 8px;
            border: solid 1px #676767;
            padding: 14px;
            font-size: 16px;

            &.error {
                border: solid 1px var(--color-primary-red);
                background-color: #fff5f3;

                &:focus {
                    outline: none;
                }
            }

        }

    }

    div.error {
        display: flex;
        align-items: center;
        margin-top: 10px;

        span.error {
            color: var(--color-primary-red);
        }

        span.svg {
            display: inline-block;
            margin-right: 10px;
        }

    }

    .view-mode {
        padding: 15px;
        border: 2px solid var(--color-primary-blue);
        border-radius: 8px;
        display: flex;
        align-items: center;

        .inner-view-mode {
            display: flex;
            flex-direction: column;

            span.value {
                color: var(--color-primary-blue);
                font-weight: 600;
                font-size: 18px;
            }

            span.editable-by {
                margin-top: 10px;
                color: var(--color-secondary-grey);
            }
        }

        span.icon {
            margin-left: auto;
            padding: 10px;
            color: var(--color-primary-blue);
            cursor: pointer;
        }
    }

</style>
<div class="edit-field">
    <label for={`input-${label}`}>{label}</label>
    {#if editMode}
        <div class="edit-mode">
            <input type="text"
                   class:error={error}
                   id={`input-${label}`}
                   bind:value={value}>
            <Button small={true} className="cancel" label={I18n.t("edit.cancel")} onClick={() => cancel()}/>
            <Button small={true} label={saveLabel} onClick={() => onSave(value)}/>
        </div>
        {#if error}
            <div class="error">
                <span class="svg">{@html critical}</span>
                <span class="error">{errorMessage}</span>
            </div>
        {/if}
    {:else}
        <div class="view-mode">
            <div class="inner-view-mode">
                <span class="value">{value}</span>
                <span class="editable-by">{editableByUser ? I18n.t("profile.editable") :
                    I18n.t("profile.nonEditable", {institution: linkedAccount.schacHomeOrganization}) }</span>
            </div>
            {#if !readOnly}
                <span class="icon" on:click={() => editMode = true}>{@html editIcon}</span>
            {/if}
        </div>
    {/if}
</div>

