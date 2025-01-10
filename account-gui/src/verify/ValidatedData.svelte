<script>
    import I18n from "../locale/I18n";
    import {institutionName, linkedAccountFamilyName, linkedAccountGivenName} from "../utils/services";
    import ValidatedField from "../verify/ValidatedField.svelte";
    import {isEmpty} from "../utils/utils";
    import {dateFromEpoch} from "../utils/date";
    import personalInfo from "../icons/verify/personalInfo.svg?raw";

    export let institution = {};

    let preferredAccount = false;

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

        .green {
            color: var(--color-primary-green);
            margin-right: 0;
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
        <h4><span class="green">{institutionName(institution)}</span>
            {I18n.t("verify.feedback.newInstitutionTitle")}</h4>
    </div>
    <p class="info">{I18n.t("verify.feedback.newInstitutionInfo")}</p>

    {#if institution.idpScoping !== "idin"}
        <ValidatedField label={I18n.t("verify.feedback.validatedGivenName")}
                        icon={preferredAccount ? personalInfo : null}
                        value={linkedAccountGivenName(institution)}/>
    {/if}

    <ValidatedField label={I18n.t("verify.feedback.validatedFamilyName")}
                    icon={preferredAccount ? personalInfo : null}
                    value={linkedAccountFamilyName(institution)}/>

    {#if !isEmpty(institution.dateOfBirth)}
        <ValidatedField label={I18n.t("verify.feedback.validatedDayOfBirth")}
                        icon={preferredAccount ? personalInfo : null}
                        value={dateFromEpoch(institution.dateOfBirth)}/>
    {/if}
</div>
