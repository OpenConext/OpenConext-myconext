<script>
    import I18n from "../locale/I18n";
    import {dateFromEpoch} from "../utils/date";
    import {onMount} from "svelte";
    import {institutionName, isStudent, linkedAccountFamilyName, linkedAccountGivenName} from "../utils/services";
    import trash from "../icons/verify/bin.svg"
    import studentIcon from "../icons/student.svg";
    import personalInfo from "../icons/verify/personalInfo.svg";
    import {isEmpty} from "../utils/utils";
    import ValidatedField from "../verify/ValidatedField.svelte";
    import {logo} from "../verify/banks";
    import studieLinkLogo from "../icons/remotecreation/studielink.png";

    export let linkedAccount;
    export let preferredAccount = false;
    export let deleteLinkedAccount;

    let expiresAt = 0;

    onMount(() => {
        expiresAt = linkedAccount.expiresAt;
    })

    const hideImage = e => {
        e.target.style.display = "none";
    }

</script>
<style lang="scss">
    .linked-account-container {
        .linked-account {
            display: flex;
            position: relative;

            .image-container {
                position: absolute;
                right: 0;

                img {
                    width: 88px;
                    height: auto;

                    &.studielink {
                        width: 144px;
                    }
                }

                :global(svg) {
                    width: 88px;
                    height: auto;
                }

            }

            div.info {
                display: flex;
                flex-direction: column;
                gap: 6px;
                margin-bottom: 15px;


                h4 {
                    color: var(--color-primary-green);
                }

                span {
                    color: var(--color-secondary-grey);
                    font-size: 14px;
                    &.expired {
                        color: var(--color-warning-red);
                    }
                }
            }


        }

        .button-container {
            display: flex;
            align-items: center;

            a {
                text-decoration: underline;
            }

            :global(svg) {
                width: 18px;
                height: auto;
                margin-right: 15px;
            }
        }
        .no-valid-information {
            font-style: italic;
            margin: 10px 0 25px 0;
        }
    }
</style>
<div class="linked-account-container">
    <div class="linked-account">
        <div class="info">
            <h4>{I18n.t("profile.from", {name: institutionName(linkedAccount)})}</h4>
            <span>{@html I18n.t("profile.receivedOnInfo", {date: dateFromEpoch(linkedAccount.createdAt)})}</span>
            <span>{@html I18n.t("profile.validUntilDateInfo", {date: dateFromEpoch(expiresAt)})}
                {#if linkedAccount.expired}
                    <span class="expired"> ({I18n.t("profile.expired")})</span>
                {/if}
            </span>
        </div>
        <div class="image-container">
            {#if !linkedAccount.external && linkedAccount.institutionGuid}
                <img src={`https://static.surfconext.nl/logos/org/${linkedAccount.institutionGuid}.png`}
                     alt="logo"
                     on:error={e => hideImage(e)}>
            {:else if linkedAccount.external && linkedAccount.idpScoping !== "studielink"}
                {@html logo(linkedAccount.issuer)}
            {:else if linkedAccount.idpScoping === "studielink"}
                <img class="studielink" src={studieLinkLogo} alt="studielink"/>
            {/if}
        </div>
    </div>
    <div class="">
        {#if !isEmpty(linkedAccountGivenName(linkedAccount))}
            <ValidatedField label={I18n.t("profile.validatedGivenName")}
                            icon={preferredAccount ? personalInfo : null}
                            readOnly={true}
                            value={linkedAccountGivenName(linkedAccount)}/>
        {/if}

        {#if !isEmpty(linkedAccountFamilyName(linkedAccount))}
            <ValidatedField label={I18n.t("profile.validatedFamilyName")}
                            icon={preferredAccount ? personalInfo : null}
                            readOnly={true}
                            value={linkedAccountFamilyName(linkedAccount)}/>
        {/if}

        {#if !isEmpty(linkedAccount.dateOfBirth)}
            <ValidatedField label={I18n.t("profile.validatedDayOfBirth")}
                            icon={preferredAccount ? personalInfo : null}
                            readOnly={true}
                            value={dateFromEpoch(linkedAccount.dateOfBirth)}/>
        {/if}

        {#if isStudent(linkedAccount)}
            <ValidatedField label={I18n.t("profile.atInstitution", {name: institutionName(linkedAccount)})}
                            icon={preferredAccount ? personalInfo : null}
                            overrideShieldIcon={studentIcon}
                            readOnly={true}
                            value={I18n.t("profile.studentRole")}/>
        {/if}

        {#if (linkedAccount.idpScoping === "idin" || isEmpty(linkedAccountGivenName(linkedAccount))) &&
        isEmpty(linkedAccount.dateOfBirth) && isEmpty(linkedAccountFamilyName(linkedAccount)) &&
        !isStudent(linkedAccount)}
            <div class="no-valid-information">{I18n.t("profile.noValidInformation", {name: institutionName(linkedAccount)})}</div>
        {/if}

    </div>
    {#if linkedAccount.idpScoping !== "studielink"}
        <div class="button-container">
            {@html trash}
            <div class="remove">
                <a href="/#"
                   on:click|preventDefault|stopPropagation={deleteLinkedAccount}>
                    {I18n.t("profile.removeLinkPrefix")}
                </a>
                <span>{I18n.t("profile.removeLinkPostfix")}</span>
            </div>
        </div>
    {/if}
</div>

