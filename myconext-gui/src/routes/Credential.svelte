<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {deletePublicKeyCredential, updatePublicKeyCredential} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import {formatCreateDate} from "../format/date";
    import Modal from "../components/Modal.svelte";

    let credential = {};
    let showModal = false;
    let name = "";

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const id = urlSearchParams.get("id");
        credential = $user.publicKeyCredentials.find(cred => cred.identifier === id)
    });

    const updateCredential = () => {
        updatePublicKeyCredential({...credential, name: name})
            .then(json => {
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                    }
                }
                navigate("/security");
                flash.setValue(I18n.t("credential.updated", {name: name}));
            });
    }

    const deleteCredential = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true;
        } else {
            deletePublicKeyCredential(credential).then(json => {
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                    }
                }
                navigate("/security");
                flash.setValue(I18n.t("credential.deleted", {name: credential.name}));
            });
        }
    }

    const cancel = () => navigate("/security");

</script>

<style lang="scss">
    .credential {
        width: 100%;
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    h2 {
        margin-top: 25px;
        color: var(--color-primary-green);
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
        display: flex;
        align-items: center;
        span.first {
            margin-right: auto;
        }

    }

</style>
<div class="credential">
    <h2>{I18n.t("credential.title")}</h2>
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
        <span class="first"><Button deletion={true} onClick={deleteCredential(true)}/></span>
        <Button small={true} className="cancel" label={I18n.t("credential.cancel")} onClick={cancel}/>
        <Button medium={true} label={I18n.t("credential.update")} onClick={updateCredential}/>
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
