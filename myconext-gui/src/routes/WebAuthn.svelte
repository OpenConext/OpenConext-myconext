<script>
    import {config} from "../stores/user";
    import I18n from "../locale/I18n";
    import {startWebAuthFlow} from "../api";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";
    import Spinner from "../components/Spinner.svelte";
    import Flash from "../components/Flash.svelte";

    let loading = false;
    let credentialName;

    const init = el => el.focus();

    const startWebAuthn = () => {
        loading = true;
        startWebAuthFlow().then(res => {
            window.location.href = `${$config.eduIDWebAuthnUrl}?token=${res.token}&name=${encodeURIComponent(credentialName)}`
        });
    }

    const cancel = () => navigate("/security");

</script>

<style>
    .web-authn {
        width: 100%;
        display: flex;
        flex-direction: column;
        height: 100%;
        padding: 15px 30px 15px 0;
    }

    h2 {
        padding: 20px 0 10px 0;
        color: var(--color-primary-green);
    }

    p.info {
        margin-top: 32px;
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
    }

</style>
<div class="web-authn">
    {#if loading}
        <Spinner/>
    {:else}
        <Flash/>
        <h2>{I18n.t("Webauthn.SetTitle.COPY")}</h2>
        <p class="info">{I18n.t("Webauthn.Info.COPY")}</p>
        <label for="credentialName">{I18n.t("webauthn.credentialName")}</label>
        <input id="credentialName"
               type="text"
               placeholder={I18n.t("Webauthn.CredentialNamePlaceholder.COPY")}
               bind:value={credentialName} use:init>

        <div class="options">
            <Button className="cancel" label={I18n.t("YourVerifiedInformation.ConfirmRemoval.Button.Cancel.COPY")} onClick={cancel}/>
            <Button label={I18n.t("Webauthn.SetUpdate.COPY")}
                    onClick={startWebAuthn}
                    disabled={!credentialName || credentialName.trim().length === 0}/>
        </div>
    {/if}

</div>