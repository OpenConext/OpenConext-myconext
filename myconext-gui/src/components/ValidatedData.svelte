<script>
    import I18n from "i18n-js";
    import {institutionName, linkedAccountFamilyName, linkedAccountGivenName} from "../utils/services";
    import ValidatedField from "../verify/ValidatedField.svelte";
    import {user} from "../stores/user";
    import {isEmpty} from "../utils/utils";
    import {dateFromEpoch} from "../utils/date";

    export let institution = {};
    export let replacement = false;

</script>
<style lang="scss">

    div.validated-data {
        display: flex;
        flex-direction: column;

        div.title {
            display: flex;
            gap: 4px;
            margin: 5px 0 15px 0;
        }

        h4 {
            &.green {
                color: var(--color-primary-green);
            }
        }

        p.info {
            margin-bottom: 25px;
        }

        :global(.validated-field) {
            margin-bottom: 28px;
        }
}

</style>
<div class="validated-data">
    <div class="title">
        <h4 class="green">{institutionName(institution)}</h4>
        <h4>{I18n.t("profile.newInstitutionTitle")}</h4>
    </div>
    <p class="info">{I18n.t(`profile.${replacement ? "preferredInstitutionInfo" : "newInstitutionInfo"}`)}</p>

    {#if institution.idpScoping !== "idin"}
        <ValidatedField label={I18n.t("profile.validatedGivenName")}
                        value={linkedAccountGivenName(institution)}/>
    {/if}

    <ValidatedField label={I18n.t("profile.validatedFamilyName")}
                    value={linkedAccountFamilyName(institution)}/>

    {#if !isEmpty(institution.dateOfBirth)}
        <ValidatedField label={I18n.t("profile.validatedDayOfBirth")}
                        value={dateFromEpoch(institution.dateOfBirth)}/>
    {/if}
</div>
