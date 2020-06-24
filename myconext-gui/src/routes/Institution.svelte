<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {me, deleteLinkedAccount} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import {formatCreateDate} from "../format/date";
    import Modal from "../components/Modal.svelte";

    let linkedAccount = {eduPersonAffiliations:[]};
    let showModal = false;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const name = urlSearchParams.get("name");
        linkedAccount = $user.linkedAccounts.find(linkedAccount => linkedAccount.schacHomeOrganization === name);
    });

    const deleteAccountLink = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true;
        } else {
            deleteLinkedAccount(linkedAccount).then(json => {
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                    }
                }
                navigate("/institutions");
                flash.setValue(I18n.t("institution.deleted", {name: linkedAccount.schacHomeOrganization}));
            });
        }
    }

    const cancel = () => navigate("/institutions");

</script>

<style>
    .institution {
        width: 100%;
        display: flex;
        height: 100%;
    }

    @media (max-width: 820px) {
        .left {
            display: none;
        }

        .inner {
            border-left: none;
        }
    }

    .header {
        display: flex;
        align-items: center;
        align-content: center;
        color: var(--color-primary-green);
    }

    .header a {
        margin-top: 8px;
    }

    h2 {
        margin-left: 25px;
    }

    p.info {
        margin: 12px 0 32px 0;
    }

    table {
        width: 100%;
    }

    td {
        border-bottom: 1px solid var(--color-primary-grey);
    }

    td.attr {
        width: 50%;
        padding: 20px;
    }

    td.value {
        width: 50%;
        font-weight: bold;
        padding-left: 20px;
    }

    div.value-container {
        display: flex;
        flex-direction: column;
    }

    div.value-container span {
        padding: 5px 0;
    }
    .options {
        margin-top: 60px;
    }


</style>
<div class="institution">
    <div class="left"></div>
    <div class="inner">
        <div class="header">
            <a href="/back" on:click|preventDefault|stopPropagation={cancel}>
                {@html chevron_left}
            </a>
            <h2>{I18n.t("institution.title")}</h2>
        </div>
        <p class="info">{I18n.t("institution.info", formatCreateDate(linkedAccount.createdAt))}</p>

        <table cellspacing="0">
            <thead></thead>
            <tbody>
            <tr class="name">
                <td class="attr">{I18n.t("institution.name")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{`${linkedAccount.schacHomeOrganization}`}</span>
                    </div>
                </td>
            </tr>
            <tr class="name">
                <td class="attr">{I18n.t("institution.eppn")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{`${linkedAccount.eduPersonPrincipalName}`}</span>
                    </div>
                </td>
            </tr>
            <tr class="name">
                <td class="attr">{I18n.t("institution.affiliations")}</td>
                <td class="value">
                    <div class="value-container">
                        {#each linkedAccount.eduPersonAffiliations as affiliation}
                            <span>{`${affiliation}`}</span>
                        {/each}
                    </div>
                </td>
            </tr>
            <tr class="name">
                <td class="attr">{I18n.t("institution.expires")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{I18n.t("institution.expiresValue", {date: formatCreateDate(linkedAccount.expiresAt).date})}</span>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>


        <div class="options">
            <Button className="cancel" label={I18n.t("institution.cancel")} onClick={cancel}/>

            <Button label={I18n.t("institution.delete")} className="cancel" onClick={deleteAccountLink(true)}/>
        </div>
    </div>

</div>

{#if showModal}
    <Modal submit={deleteAccountLink(false)}
           cancel={() => showModal = false}
           question={I18n.t("institution.deleteInstitutionConfirmation")}
                   title={I18n.t("institution.deleteInstitution")}>
    </Modal>
{/if}
