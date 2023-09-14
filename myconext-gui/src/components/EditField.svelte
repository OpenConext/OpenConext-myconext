<script>
    import editIcon from "../icons/edit.svg";
    import Button from "./Button.svelte";
    import I18n from "i18n-js";


    export let initialValue;
    export let onSave;
    export let label;
    export let editableByUser = true;
    export let readOnly = false;

    let editMode = false;
    let value = initialValue;

    const cancel = () => {
        editMode = false;
        value = initialValue;
    }

</script>
<style lang="scss">

</style>
<div class="edit-field">
    <label for={`input-${name}`}>{label}</label>
    {#if editMode}
        <div class="edit-mode">
            <input type="text" id={`input-${name}`} bind:value={value}>
            <div class="actions">
                <Button className="cancel" label={I18n.t("edit.cancel")} onClick={() => cancel()}/>
                <Button label={I18n.t("edit.save")} onClick={() => onSave()}/>
            </div>
        </div>
    {:else}
        <div class="view-mode">
            <span>{value}</span>
            <span class="editable-by">{editableByUser ? "nice" : "nice2"}</span>
            {@html editIcon}
        </div>
    {/if}
</div>


</span>
</label>
