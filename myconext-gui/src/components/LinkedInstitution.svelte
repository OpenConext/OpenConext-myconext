<script>
    import I18n from "../locale/I18n";
    import {dateFromEpoch} from "../utils/date";
    import {onMount} from "svelte";
    import {isEmpty} from "../utils/utils";
    import {institutionName} from "../utils/services";

    export let linkedAccount;
    export let manageVerifiedInformation;
    export let roleContext;
    export let includeAffiliations = false;

    let affiliations = [];
    let expiresAt = 0;

    onMount(() => {
        if (!isEmpty(linkedAccount.eduPersonAffiliations)) {
            affiliations = Array.from(new Set(linkedAccount.eduPersonAffiliations));
        } else if (!isEmpty(linkedAccount.affiliations)) {
            affiliations = Array.from(new Set(linkedAccount.affiliations));
        }
        expiresAt = linkedAccount.expiresAt;
    })

</script>
<style lang="scss">
    .linked-institution-container {
        margin-top: 15px;
        border-top: 2px solid var(--color-primary-blue);
        padding-top: 15px;
    }

    .linked-institution {
        padding-left: 40px;

        a {
            text-decoration: underline;
        }
    }

    p {
        font-weight: 600;
        margin-bottom: 6px;

        &.details {
            margin-top: 12px;
        }
    }

    ul {
        list-style-type: disc;
        list-style-position: inside;
    }
    .button-container {
        margin-top: 15px;
        display: flex;
        gap: 25px;
    }

</style>
<div class="linked-institution-container">

    <div class="linked-institution">
        <p>{I18n.t("Profile.VerifiedBy.COPY", {name: institutionName(linkedAccount)})}</p>
        <ul>
            <li>{@html I18n.t("profile.receivedOn", {date: dateFromEpoch(linkedAccount.createdAt)})}</li>
            <li>{@html I18n.t("profile.validUntilDate", {date: dateFromEpoch(expiresAt)})}</li>
        </ul>
        {#if includeAffiliations && !isEmpty(affiliations)}
            <p class="details">{I18n.t("Profile.Affiliations.COPY")}</p>
            <ul>
                {#each affiliations as aff}
                    <li>{aff}</li>
                {/each}
            </ul>
        {/if}
        {#if roleContext && (linkedAccount.subjectId || linkedAccount.eduPersonPrincipalName)}
            {#if linkedAccount.subjectId && !linkedAccount.external}
                <p class="details">{I18n.t("profile.subjectId")}</p>
                <ul>
                    <li>{linkedAccount.subjectId}</li>
                </ul>
            {:else if linkedAccount.eduPersonPrincipalName}
                <p class="details">{I18n.t("profile.eppn")}</p>
                <ul>
                    <li>{linkedAccount.eduPersonPrincipalName}</li>
                </ul>
            {/if}
        {/if}
        <div class="button-container">
            <a class="manage-information"
               href="/#"
               on:click|preventDefault|stopPropagation={manageVerifiedInformation}>
                {I18n.t("Profile.ManageYourVerifiedInformation.COPY")}
            </a>
        </div>
    </div>
</div>
