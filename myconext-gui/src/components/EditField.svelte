<script>
    import editIcon from "../icons/edit.svg";
    import shieldIcon from "../icons/redesign/shield-full.svg";
    import chevronUpIcon from "../icons/chevron-up.svg";
    import Button from "./Button.svelte";
    import I18n from "i18n-js";
    import critical from "../icons/critical.svg";
    import LinkedInstitution from "./LinkedInstitution.svelte";
    import {institutionName} from "../utils/services";

    const cancel = () => {
        value = firstValue;
        if (onCancel) {
            onCancel();
        } else {
            editMode = false;
        }
    }

    export let firstValue;
    export let linkedAccount;
    export let onSave;
    export let onCancel;
    export let editMode = false;
    export let onEdit;
    export let label;
    export let editableByUser = true;
    export let error = false;
    export let errorMessage;
    export let nameField = true;
    export let saveLabel = I18n.t("edit.save");
    export let addInstitution;
    export let editHint;

    let value = firstValue;
    let showDropDown = false;

</script>
<style lang="scss">

    .edit-field {
        margin-bottom: 15px;

        &:not(.name-field) {
            margin-top: 40px;
        }
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

    .edit-hint {
        margin-top: 8px;
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

    .view-mode-container {
        padding: 15px;
        border: 2px solid var(--color-primary-blue);
        border-radius: 8px;
        display: flex;
        flex-direction: column;
    }

    .view-mode {
        display: flex;
        align-items: center;

        .inner-view-mode {
            display: flex;
            flex-direction: column;

            span.value {
                color: var(--color-primary-blue);
                font-weight: 600;
                font-size: 18px;
                display: flex;
                align-items: center;

                span.shield {
                    margin-right: 8px;

                    :global(svg) {
                        width: 16px;
                        height: auto;
                    }
                }
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
            fill: var(--color-primary-blue);
            cursor: pointer;

            &.show-drop-down {
                :global( svg) {
                    transform: rotate(180deg);
                }

            }
        }
    }

</style>
<div class="edit-field" class:name-field={nameField}>
    <label for={`input-${label}`}>{label}</label>
    {#if editMode || error}
        <div class="edit-mode">
            <input type="text"
                   class:error={error}
                   id={`input-${label}`}
                   bind:value={value}>
            <Button small={true}
                    className="cancel"
                    label={I18n.t("edit.cancel")}
                    onClick={() => cancel()}/>
            <Button small={true}
                    label={saveLabel}
                    disabled={!value || !value.trim()}
                    onClick={() => onSave(value)}/>
        </div>
        {#if editHint}
            <p class="edit-hint">{editHint}</p>
        {/if}
    {:else}
        <div class="view-mode-container">
            <div class="view-mode">
                <div class="inner-view-mode">
                <span class="value">
                    {#if !editableByUser}
                    <span class="shield">{@html shieldIcon}</span>
                {/if}
                    {firstValue}</span>
                    <span class="editable-by">{editableByUser ? I18n.t("profile.editable") :
                        I18n.t("profile.nonEditable", {name: institutionName(linkedAccount)}) }</span>
                </div>
                {#if editableByUser}
                    <span class="icon" on:click={() => onEdit()}>{@html editIcon}</span>
                {:else}
                <span class="icon" class:show-drop-down={!showDropDown}
                      on:click={() => showDropDown = !showDropDown}>{@html chevronUpIcon}</span>
                {/if}
            </div>
            {#if showDropDown}
                <LinkedInstitution linkedAccount={linkedAccount}
                                   includeAffiliations={false}
                                   roleContext={false}
                                   addInstitution={addInstitution}/>
            {/if}

        </div>
    {/if}
    {#if error}
        <div class="error">
            <span class="svg">{@html critical}</span>
            <span class="error">{errorMessage}</span>
        </div>
    {/if}
</div>

