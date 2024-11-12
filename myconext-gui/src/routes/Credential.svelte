<script>
    import {flash, user} from "../stores/user";
    import I18n from "../locale/I18n";
    import {deletePublicKeyCredential, testWebAutnUrl, updatePublicKeyCredential} from "../api";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import Modal from "../components/Modal.svelte";

    let credential = {};
    let showModal = false;
    let name = "";

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const id = urlSearchParams.get("id");
        credential = $user.publicKeyCredentials.find(cred => cred.identifier === id);
        name = credential.name;
        const testWebAuthn = urlSearchParams.get("success");
        if (testWebAuthn) {
            flash.setValue(I18n.t("webauthn.testFlash"), 3750);
        }
    });

    const startTestFlow = () => {
        testWebAutnUrl().then(res => {
            window.location.href = res.url;
        });
    }

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
        margin-top: 35px;
        color: var(--color-primary-green);
    }

    label {
      font-weight: bold;
      margin: 33px 0 13px 0;
      display: inline-block;
    }

    input {
      border-radius: 8px;
      border: solid 1px #676767;
      padding: 14px;
      font-size: 16px;
    }

    .options {
        margin-top: 60px;
        display: flex;
        align-items: center;
        span.first {
            margin-right: auto;
        }

        @media (max-width: 1080px) {
            align-items: normal;
        }
    }

</style>
<div class="credential">
    <h2>{I18n.t("credential.title")}</h2>
    <label for="credentialName">{I18n.t("credential.name")}</label>
    <input id="credentialName" type="text" bind:value={name}>

    <div class="options">
        <span class="first">
            <Button deletion={true}
                    onClick={deleteCredential(true)}/></span>
        <Button className="cancel"
                label={I18n.t("credential.cancel")}
                onClick={cancel}/>
        <Button label={I18n.t("security.test")}
                className="ghost"
                onClick={startTestFlow}/>
        <Button label={I18n.t("credential.update")}
                disabled={!name || name.trim().length === 0}
                onClick={updateCredential}/>
    </div>
</div>

{#if showModal}
    <Modal submit={deleteCredential(false)}
           cancel={() => showModal = false}
           warning={true}
           confirmTitle={I18n.t("modal.delete")}
           question={I18n.t("credential.deleteCredentialConfirmation", {name: credential.name})}
           title={I18n.t("credential.deleteCredential")}>
    </Modal>
{/if}
