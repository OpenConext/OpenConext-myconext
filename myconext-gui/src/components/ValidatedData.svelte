<script>
    import I18n from "../locale/I18n";
    import {
        affiliation,
        institutionLogo,
        institutionName,
        linkedAccountFamilyName,
        linkedAccountGivenName
    } from "../utils/services";
    import ValidatedField from "../verify/ValidatedField.svelte";
    import {isEmpty} from "../utils/utils";
    import {dateFromEpoch} from "../utils/date";
    import personalInfo from "../icons/verify/personalInfo.svg?raw";

    export let institution = {};
    export let replacement = false;
    export let preferredAccount = false;
    export let readOnly = false;

</script>
<style lang="scss">

    div.validated-data {
        display: flex;
        flex-direction: column;

        div.title {
            display: flex;
            gap: 6px;
            margin: 5px 0 15px 0;
            align-items: center;
            span.icon {
                font-size: 20px;
            }
        }

        h4 {
            &.green {
                color: var(--color-primary-green);
            }
        }

        p.info {
            margin-bottom: 25px;

            &.border {
                padding-top: 20px;
                border-top: 1px solid var(--color-primary-grey);
            }
        }

        :global(.validated-field) {
            margin-bottom: 28px;
        }
}

</style>
<div class="validated-data">
    <div class="title">
        <h4 class="green">{I18n.t("Profile.Title.COPY")}</h4>
        <span class="icon">🎉</span>
    </div>
    <p class="info">{I18n.t("NameUpdated.Description.COPY")}</p>
    <ValidatedField label={affiliation(institution)}
                    overrideShieldIcon={institutionLogo(institution)}
                    readOnly={true}
                    isExternal={institution.external}
                    value={institutionName(institution)}/>
    <p class="info border">{I18n.t(`profile.${replacement ? "preferredInstitutionInfo" : "newInstitutionInfoAttributes"}`)}</p>

    <ValidatedField label={I18n.t("Profile.VerifiedGivenName.COPY")}
                    icon={preferredAccount ? personalInfo : null}
                    readOnly={readOnly}
                    value={linkedAccountGivenName(institution)}/>

    <ValidatedField label={I18n.t("Profile.VerifiedFamilyName.COPY")}
                    icon={preferredAccount ? personalInfo : null}
                    readOnly={readOnly}
                    value={linkedAccountFamilyName(institution)}/>

    {#if !isEmpty(institution.dateOfBirth)}
        <ValidatedField label={I18n.t("Profile.VerifiedDateOfBirth.COPY")}
                        readOnly={readOnly}
                        icon={preferredAccount ? personalInfo : null}
                        value={dateFromEpoch(institution.dateOfBirth)}/>
    {/if}

    <!--{#if !institution.external && !isEmpty(institution.eduPersonAffiliations)}-->
    <!--    <ValidatedField label={I18n.t("Profile.Validated.COPYAffiliations")}-->
    <!--                    readOnly={true}-->
    <!--                    icon={preferredAccount ? personalInfo : null}-->
    <!--                    value={institution.eduPersonAffiliations.join(", ")}/>-->
    <!--{/if}-->
</div>
