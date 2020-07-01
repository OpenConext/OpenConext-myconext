<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {deletePublicKeyCredential} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import {formatCreateDate} from "../format/date";
    import Modal from "../components/Modal.svelte";

    let credential = {};
    let showModal = false;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const id = urlSearchParams.get("id");
        credential = $user.publicKeyCredentials.find(cred => cred.identifier === id)
    });

    const deleteCredential = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true;
        } else {
            deletePublicKeyCredential(credential.identifier).then(json => {
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                    }
                }
                navigate("/webauthn");
                flash.setValue(I18n.t("credential.deleted", {name: credential.name}));
            });
        }
    }

    const cancel = () => navigate("/webauthn");

</script>

<style>
    .credential {
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
        width: 30%;
        padding: 20px;
    }

    td.value {
        width: 70%;
        font-weight: bold;
        padding-left: 20px;
    }

    div.value-container {
        display: flex;
        flex-direction: column;
    }

    div.value-container span {
        word-break: break-word;
        padding: 5px 0;
    }

    .options {
        margin-top: 60px;
    }

</style>
<div class="credential">
    <div class="left"></div>
    <div class="inner">
        <div class="header">
            <a href="/back" on:click|preventDefault|stopPropagation={cancel}>
                {@html chevron_left}
            </a>
            <h2>{I18n.t("credential.title")}</h2>
        </div>
        <p class="info">{I18n.t("credential.info", formatCreateDate(credential.createdAt))}</p>

        <table cellspacing="0">
            <thead></thead>
            <tbody>
            <tr class="name">
                <td class="attr">{I18n.t("credential.name")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{`${credential.name}`}</span>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>


        <div class="options">
            <Button className="cancel" label={I18n.t("credential.cancel")} onClick={cancel}/>
            <Button label={I18n.t("credential.delete")} className="cancel" onClick={deleteCredential(true)}/>
        </div>
    </div>

</div>

{#if showModal}
    <Modal submit={deleteCredential(false)}
           cancel={() => showModal = false}
           warning={true}
           question={I18n.t("credential.deleteCredentialConfirmation", {name: credential.name})}
                   title={I18n.t("credential.deleteCredential")}>
    </Modal>
{/if}
