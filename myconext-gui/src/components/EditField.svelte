<script>
    import editIcon from "../icons/edit.svg?raw";
    import shieldIcon from "../icons/redesign/shield-full.svg?raw";
    import chevronUpIcon from "../icons/chevron-up.svg?raw";
    import Button from "./Button.svelte";
    import I18n from "../locale/I18n";
    import critical from "../icons/critical.svg?raw";
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
    export let editLabel;
    export let editableByUser = true;
    export let error = false;
    export let errorMessage;
    export let nameField = true;
    export let saveLabel = I18n.t("Email.Save.COPY");
    export let manageVerifiedInformation;
    export let editHint;

    let value = firstValue;
    let showDropDown = false;

    const toggle = () => {
        if (editableByUser) {
            onEdit();
        } else {
            showDropDown = !showDropDown
        }

    }

</script>
<style lang="scss">
    $max-width-not-edit: 480px;
    $max-width-mobile: 1080px;

    .edit-field {
        margin-bottom: 15px;
        max-width: $max-width-not-edit;

        &:hover:not(.show-edit-mode) {
            background-color: #f0f8ff;
        }

        &.show-drop-down {
            background-color: #f0f8ff;
        }

        &.show-edit-mode {
            max-width: 100%;

        }

        @media (max-width: $max-width-mobile) {
            margin: 0 0 15px 0;
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

        @media (max-width: $max-width-mobile) {
            flex-direction: column;
        }

        input {
            width: $max-width-not-edit;
            border-radius: 8px;
            border: solid 1px #676767;
            padding: 14px;
            font-size: 16px;
            @media (max-width: $max-width-mobile) {
                width: 100%;
            }
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
        padding: 14px;
        border: 2px solid var(--color-primary-blue);
        border-radius: 8px;
        display: flex;
        flex-direction: column;
    }

    .view-mode {
        display: flex;
        align-items: center;
        cursor: pointer;

        .inner-view-mode {
            display: flex;
            flex-direction: column;

            div.value {
                color: var(--color-primary-blue);
                font-weight: 600;
                font-size: 18px;
                display: flex;
                align-items: center;

                .values {
                    display: flex;
                    flex-direction: column;
                }
                span.shield {
                    margin-right: 12px;

                    :global(svg) {
                        width: 28px;
                        height: auto;
                    }
                }
            }

            span.editable-by {
                margin-top: 2px;
                color: var(--color-secondary-grey);
                font-size: 14px;
                font-weight: 100;
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
<div class="edit-field"
     class:name-field={nameField}
     class:show-drop-down={showDropDown}
     class:show-edit-mode={editMode}>
    {#if label}
        <label for={`input-${label}`}>{label}</label>
    {/if}
    {#if editMode || error}
        <div class="edit-mode">
            <input type="text"
                   class:error={error}
                   id={`input-${label}`}
                   bind:value={value}>
            <Button small={true}
                    className="cancel"
                    label={I18n.t("YourVerifiedInformation.ConfirmRemoval.Button.Cancel.COPY")}
                    onClick={() => cancel()}/>
            <Button small={true}
                    label={saveLabel}
                    disabled={!value || !value.trim() || firstValue === value}
                    onClick={() => onSave(value)}/>
        </div>
        {#if editHint}
            <p class="edit-hint">{editHint}</p>
        {/if}
    {:else}
        <div class="view-mode-container">
            <div class="view-mode" on:click={toggle}>
                <div class="inner-view-mode">
                    <div class="value">
                    {#if !editableByUser}
                        <span class="shield">{@html shieldIcon}</span>
                    {/if}
                        <div class="values">
                            <span>{firstValue}</span>
                    {#if editLabel}
                        <span class="editable-by">{editLabel}</span>
                    {:else}
                        <span class="editable-by">{editableByUser ? I18n.t("profile.editable") :
                        I18n.t("profile.nonEditable", {name: institutionName(linkedAccount)}) }</span>
                    {/if}
                        </div>
                    </div>
                </div>
                {#if editableByUser}
                    <span class="icon" on:click={() => onEdit()}>{@html editIcon}</span>
                {:else}
                <span class="icon" class:show-drop-down={!showDropDown}>
                    {@html chevronUpIcon}
                </span>
                {/if}
            </div>
            {#if showDropDown}
                <LinkedInstitution linkedAccount={linkedAccount}
                                   includeAffiliations={false}
                                   roleContext={false}
                                   manageVerifiedInformation={manageVerifiedInformation}/>
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

