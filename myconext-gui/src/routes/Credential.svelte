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
        name = Credential.Name.COPY;
        const testWebAuthn = urlSearchParams.get("success");
        if (testWebAuthn) {
            flash.setValue(I18n.t("Webauthn.TestFlash.COPY"), 3750);
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
                flash.setValue(I18n.t("Edit.Update.COPYd", {name: name}));
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
                flash.setValue(I18n.t("credential.deleted", {name: Credential.Name.COPY}));
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
      font-weight: 600;
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
    <h2>{I18n.t("Credential.Title.COPY")}</h2>
    <label for="credentialName">{I18n.t("Credential.Name.COPY")}</label>
    <input id="credentialName" type="text" bind:value={name}>

    <div class="options">
        <span class="first">
            <Button deletion={true}
                    onClick={deleteCredential(true)}/></span>
        <Button className="cancel"
                label={I18n.t("YourVerifiedInformation.ConfirmRemoval.Button.Cancel.COPY")}
                onClick={cancel}/>
        <Button label={I18n.t("Webauthn.Test.COPY")}
                className="ghost"
                onClick={startTestFlow}/>
        <Button label={I18n.t("Edit.Update.COPY")}
                disabled={!name || name.trim().length === 0}
                onClick={updateCredential}/>
    </div>
</div>

{#if showModal}
    <Modal submit={deleteCredential(false)}
           cancel={() => showModal = false}
           warning={true}
           confirmTitle={I18n.t("YourVerifiedInformation.ConfirmRemoval.Button.YesDelete.COPY")}
           question={I18n.t("Credential.DeleteCredential.COPYConfirmation", {name: Credential.Name.COPY})}
           title={I18n.t("Credential.DeleteCredential.COPY")}>
    </Modal>
{/if}
