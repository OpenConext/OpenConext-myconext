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
    export let includeAffiliations = false;

    let affiliations;

    onMount(() => {
        affiliations = Array.from(new Set(linkedAccount.eduPersonAffiliations));
    })

</script>
<style lang="scss">

    table {
        margin-top: 25px;
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
        <tr>
            <td>
                {I18n.t("profile.validUntil")}
            </td>
            <td class="value">{dateFromEpoch(linkedAccount.expiresAtNonValidated || linkedAccount.expiresAt)}</td>
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
