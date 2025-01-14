<script>
    import chevronUpIcon from "../icons/chevron-up.svg?raw";
    import I18n from "../locale/I18n";
    import LinkedInstitution from "./LinkedInstitution.svelte";
    import notFound from "../icons/school-building.svg?raw";
    import {onMount} from "svelte";
    import {splitListSemantically} from "../utils/utils";
    import {institutionName} from "../utils/services";

    export let linkedAccount;
    export let manageVerifiedInformation;

    let affiliations;
    let showDropDown = false;

    onMount(() => {
        const eduPersonAffiliations = (linkedAccount.eduPersonAffiliations || [])
            .map(affiliation => {
                const capitalized = affiliation.charAt(0).toUpperCase() + affiliation.slice(1);
                const indexOf = capitalized.indexOf("@");
                return indexOf > -1 ? capitalized.substring(0, indexOf) : capitalized;
            })
        affiliations = splitListSemantically(Array.from(new Set(eduPersonAffiliations)), I18n.t("edit.and"));
    })


</script>
<style lang="scss">

    .institution-role {
        margin-bottom: 15px;
        padding: 15px;
        border: 2px solid var(--color-primary-blue);
        border-radius: 8px;
        display: flex;
        flex-direction: column;


        &:hover:not(.show-edit-mode) {
            background-color: #f0f8ff;
        }

        &.show-drop-down {
            background-color: #f0f8ff;
        }

        &.expired {
            background-color: #f6f6f6;
            border: 2px solid var(--color-primary-grey);
        }

        span.expired {
            color: var(--color-primary-red);
        }
    }

    .affiliation {
        display: flex;
        align-items: center;
        cursor: pointer;

        span.student {
            margin-right: 12px;

            img {
                width: 48px;
                height: auto;
            }

            :global( svg) {
                width: 48px;
                height: auto;
            }
        }

        div.role {
            display: flex;
            flex-direction: column;

            p {
                font-weight: 600;
                margin-bottom: 10px;
                display: inline-block;
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
<div class="institution-role"
     class:show-drop-down={showDropDown}
     class:expired={linkedAccount.expired}>
    <div class="affiliation" on:click={() => showDropDown = !showDropDown}>
        <span class="student">
        {#if linkedAccount.logoUrl}
            <img src={linkedAccount.logoUrl} alt="logo">
            {:else}
            {@html notFound}
        {/if}
        </span>
        <div class="role">
            <p>{affiliations || I18n.t("Profile.Student.COPYRole")}
                {#if linkedAccount.expired}
                    <span class="expired">{` (${I18n.t("profile.expired")})`}</span>
                {/if}
            </p>
            <span>{I18n.t("Profile.InstitutionAt.COPY", {name: institutionName(linkedAccount)})}</span>
        </div>

        <span class="icon" class:show-drop-down={!showDropDown}>
            {@html chevronUpIcon}
        </span>
    </div>
    {#if showDropDown}
        <LinkedInstitution linkedAccount={linkedAccount}
                           includeAffiliations={true}
                           roleContext={true}
                           manageVerifiedInformation={manageVerifiedInformation}/>
    {/if}

</div>

