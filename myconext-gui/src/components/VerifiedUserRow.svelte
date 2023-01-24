<script>
    import verifiedSvg from "../icons/redesign/shield-full.svg";
    import nonVerifiedSvg from "../icons/redesign/shield-empty.svg";
    import chevronDownSvg from "../icons/chevron-down.svg";
    import chevronUpSvg from "../icons/chevron-up.svg";
    import Button from "../components/Button.svelte";
    import {deleteLinkedAccount, startLinkAccountFlow} from "../api";
    import Modal from "../components/Modal.svelte";
    import {formatCreateDate} from "../format/date";
    import I18n from "i18n-js";
    import {flash, user} from "../stores/user";

    export let showDetails = false;
    export let attr = "";
    export let verified = false;
    export let account = {};
    export let verifiedValue = "";
    export let info = "";
    export let verifyType = "";
    export let buttonTxt = "";
    export let expiresAtAttributeName = "expiresAtNonValidated";
    export let refresh = () => true;

    let showModal = false;
    let showDeleteModal = false;

    const addInstitution = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true
        } else {
            startLinkAccountFlow().then(json => {
                window.location.href = json.url;
            });
        }
    }

    const deleteAccountLink = showConfirmation => {
        if (showConfirmation) {
            showDeleteModal = true;
        } else {
            showDeleteModal = false;
            deleteLinkedAccount(account).then(json => {
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                    }
                }
                refresh();
                flash.setValue(I18n.t("institution.deleted", {name: account.schacHomeOrganization}));
            });
        }
    }

    const toggleDetails = () => {
        if (verified) {
            showDetails = !showDetails;
        }
    }

</script>
<style lang="scss">
    div.value-container {
        display: flex;
        align-items: center;
        width: 100%;

        a.toggle-link {
            margin-left: auto;
        }

        span {
            word-break: break-word;

            &.info {
                max-width: 270px;
                font-weight: normal;
                margin: 5px 15px 5px 0;
            }
        }
    }

    :global(div.value-container a.toggle-link svg) {
        fill: var(--color-secondary-grey);
        width: 30px;
        height: auto;
    }

    tr.full {
        background-color: var(--color-background);
    }

    tr.verified {
        cursor: pointer;

        &:hover {
            background-color: var(--color-background);
        }
    }

    td {
        border-bottom: 1px solid var(--color-primary-grey)
    }

    td.verified-at {
        font-weight: normal;
    }

    td.verified {
        width: 10%;
        text-align: center;
    }

    td.attr {
        width: 30%;
        padding: 20px;
        font-weight: normal;

        &.inner-value {
            width: 50%;
        }
    }

    td.value {
        width: 70%;
        font-weight: bold;
        padding-left: 20px;

        &.inner-value {
            width: 50%;
        }

        &.details {
            padding-left: 0;
        }

        table {
            width: 100%;
            background-color: var(--color-background);

            tr:last-child {
                td {
                    border-bottom: none;
                }
            }

            td {
                padding: 15px 10px;
            }
        }
    }


    :global(td.verified svg) {
        width: 22px;
        height: auto;
    }

    @media (max-width: 820px) {
        div.value-container {
            flex-direction: column;
            align-items: flex-start;

        }
        td.verified {
            display: none;
        }
    }

    :global(div.value-container a.button) {
        margin-left: auto;

        @media (max-width: 820px) {
            margin-top: 15px;
            margin-left: 0;
        }
    }

</style>

<tr class:full={showDetails} class:verified={verified} on:click={toggleDetails}>
    <td class="attr">{attr}</td>
    <td class="verified">{@html verified ? verifiedSvg : nonVerifiedSvg}</td>
    <td class="value">
        <div class="value-container">
            {#if verified}
                <span>{verifiedValue}</span>
                <a class="toggle-link" href="/"
                   on:click|preventDefault|stopPropagation={toggleDetails}>
                    {@html showDetails ? chevronUpSvg : chevronDownSvg}
                </a>
            {:else}
                <span class="info">{info}</span>
                <Button href={`/${buttonTxt}`} label={buttonTxt} onClick={addInstitution(true)}
                        small={true}/>
            {/if}

        </div>
    </td>
</tr>
{#if showDetails}
    <tr class="full">
        <td class="attr"></td>
        <td class="verified"></td>
        <td class="value details">
            <table class="inner-details">
                <thead></thead>
                <tbody>
                <tr>
                    <td colspan="2" class="verified-at">
                        {@html I18n.t("profile.verifiedAt", {
                            ...formatCreateDate(account.createdAt),
                            name: account.schacHomeOrganization
                        })}
                    </td>
                </tr>
                <tr>
                    <td class="attr inner-value">{I18n.t("profile.institution")}</td>
                    <td class="value inner-value">{account.schacHomeOrganization}</td>
                </tr>
                <tr>
                    <td class="attr inner-value">{I18n.t("profile.affiliations")}</td>
                    <td class="value inner-value">{(account.eduPersonAffiliations || []).join(", ")}</td>
                </tr>
                <tr>
                    <td class="attr inner-value">{I18n.t("profile.expires")}</td>
                    <td class="value inner-value">
                        {I18n.t("profile.expiresValue", formatCreateDate(account[expiresAtAttributeName]))}
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <Button label={I18n.t("institution.delete")}
                                large={true}
                                inline={true}
                                onClick={() => deleteAccountLink(true)}/>
                    </td>
                </tr>
                </tbody>
            </table>

        </td>
    </tr>
{/if}

{#if showModal}
    <Modal submit={addInstitution(false)}
           cancel={() => showModal = false}
           question={I18n.t(`profile.${verifyType}.addInstitutionConfirmation`)}
           title={I18n.t(`profile.${verifyType}.addInstitution`)}
           confirmTitle={I18n.t("profile.proceed")}>
    </Modal>
{/if}

{#if showDeleteModal}
    <Modal submit={() => deleteAccountLink(false)}
           cancel={() => showDeleteModal = false}
           warning={true}
           question={I18n.t("institution.deleteInstitutionConfirmation")}
           title={I18n.t("institution.deleteInstitution")}>
    </Modal>
{/if}
