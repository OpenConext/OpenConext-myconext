<script>
    import Button from "./Button.svelte";
    import I18n from "i18n-js";
    import {dateFromEpoch} from "../utils/date";
    import {onMount} from "svelte";
    import {isEmpty} from "../utils/utils";
    import {institutionName} from "../utils/services";

    export let linkedAccount;
    export let addInstitution;
    export let removeInstitution;
    export let roleContext;
    export let includeAffiliations = false;

    let affiliations;

    onMount(() => {
        affiliations = Array.from(new Set(linkedAccount.eduPersonAffiliations));
    })

</script>
<style lang="scss">
    .linked-institution {
        margin-top: 15px;
        border-top: 2px solid var(--color-primary-blue);
        padding-top: 15px;
    }
    table {
        border-collapse: collapse;

        tr {
            border-bottom: 1px solid var(--color-primary-grey);

            td {
                padding: 12px 5px;
                width: 35%;

                &.value {
                    font-weight: 600;
                    width: 65%;

                    span {
                        display: block;
                        margin-top: 4px;
                    }
                }
            }
        }
    }

    .button-container {
        margin-top: 25px;
        display: flex;
        gap: 25px;
    }

</style>
<div class="linked-institution">
    <table>
        <thead/>
        <tbody>
        <tr>
            <td colspan="2">
                {@html I18n.t("profile.verifiedBy", {
                    name: institutionName(linkedAccount),
                    date: dateFromEpoch(linkedAccount.createdAt)
                })}
            </td>
        </tr>
        <tr>
            <td>
                {I18n.t("profile.institution")}
            </td>
            <td class="value">{institutionName(linkedAccount)}</td>
        </tr>
        {#if includeAffiliations && !isEmpty(affiliations)}
            <tr>
                <td>
                    {I18n.t("profile.affiliations")}
                </td>
                <td class="value">
                    {#each affiliations as aff}
                        <span>{aff}</span>
                    {/each}
                </td>
            </tr>
        {/if}
        {#if linkedAccount.subjectId || linkedAccount.eduPersonPrincipalName}
            {#if linkedAccount.subjectId}
                <tr>
                    <td>
                        {I18n.t("profile.subjectId")}
                    </td>
                    <td class="value">
                        {linkedAccount.subjectId}
                    </td>
                </tr>
            {:else}
                <tr>
                    <td>
                        {I18n.t("profile.eppn")}
                    </td>
                    <td class="value">
                        {linkedAccount.eduPersonPrincipalName}
                    </td>
                </tr>
            {/if}
        {/if}
        <tr>
            <td>
                {I18n.t("profile.validUntil")}
            </td>
            {#if roleContext}
                <td class="value">{dateFromEpoch(linkedAccount.expiresAtRole)}</td>
            {:else}
                <td class="value">{dateFromEpoch(linkedAccount.expiresAtNonValidated || linkedAccount.expiresAt)}</td>
            {/if}
        </tr>
        </tbody>
    </table>
    <div class="button-container">
        <Button label={I18n.t("edit.update")}
                inline={true}
                small={true}
                onClick={() => addInstitution(true)}/>
        {#if removeInstitution}
            <Button label={I18n.t("profile.remove")}
                    inline={true}
                    small={true}
                    onClick={() => removeInstitution(true, linkedAccount)}/>
        {/if}
    </div>
</div>
