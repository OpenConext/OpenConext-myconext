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

    let linkedAccount = {};
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
                flash.setValue(I18n.ts("institution.deleted", {name: linkedAccount.schacHomeOrganization}));
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

    .left {
        background-color: #f3f6f8;
        width: 270px;
        height: 100%;
        border-bottom-left-radius: 8px;
    }

    .inner {
        margin: 20px 0 190px 0;
        padding: 15px 15px 0 40px;
        border-left: 2px solid var(--color-primary-grey);
        display: flex;
        flex-direction: column;
        background-color: white;
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
        align-items: center;
    }

    .options {
        margin-top: 60px;
    }

    :global(.options a:not(:first-child)) {
        margin-left: 25px;
    }


</style>
<div class="institution">
    <div class="left"></div>
    <div class="inner">
        <div class="header">
            <a href="/back" on:click|preventDefault|stopPropagation={cancel}>
                {@html chevron_left}
            </a>
            <h2>{I18n.ts("institution.title")}</h2>
        </div>
        <p class="info">{I18n.t("institution.info", formatCreateDate(linkedAccount.createdAt))}</p>

        <table cellspacing="0">
            <thead></thead>
            <tbody>
            <tr class="name">
                <td class="attr">{I18n.ts("institution.name")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{`${linkedAccount.schacHomeOrganization}`}</span>
                    </div>
                </td>
            </tr>
            <tr class="name">
                <td class="attr">{I18n.ts("institution.eppn")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{`${linkedAccount.eduPersonPrincipalName}`}</span>
                    </div>
                </td>
            </tr>
            <tr class="name">
                <td class="attr">{I18n.ts("institution.expires")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{I18n.ts("institution.expiresValue",formatCreateDate(linkedAccount.expiresAt))}</span>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>


        <div class="options">
            <Button className="cancel" label={I18n.ts("institution.cancel")} onClick={cancel}/>

            <Button label={I18n.ts("institution.delete")} className="cancel" onClick={deleteAccountLink(true)}/>
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
