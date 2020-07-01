<script>
    import {config, user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {startWebAuthFlow} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";
    import Spinner from "../components/Spinner.svelte";
    import chevron_right from "../icons/chevron-right.svg";

    let usePublicKey = $user.usePublicKey;
    let loading = false;
    let credentialName;

    const startWebAuthn = () => {
        loading = true;
        startWebAuthFlow().then(res => {
            window.location.href = `${$config.eduIDWebAuthnUrl}?token=${res.token}&name=${encodeURIComponent(credentialName)}`
        });
    }

    const credentialsDetails = credential => () =>
            navigate(`/credential?id=${encodeURIComponent(credential.identifier)}`);

    const cancel = () => navigate("/security");

</script>

<style>
    .web-authn {
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
        margin-top: 32px;
    }

    p.info2 {
        margin-top: 22px;
        margin-bottom: 32px;
    }

    p.keys {
        margin-top: 22px;
    }

    table {
        margin-bottom: 30px;
        width: 100%;
    }

    tr {
        cursor: pointer;
    }

    td {
        border-bottom: 1px solid var(--color-primary-grey);
    }

    td.value {
        width: 70%;
        font-weight: bold;
        padding: 20px;
    }

    div.value-container {
        display: flex;
        align-items: center;
    }

    div.value-container span {
        word-break: break-word;
    }

    div.value-container a.menu-link {
        margin-left: auto;
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
    }

</style>
<div class="web-authn">
    {#if loading}
        <Spinner/>
    {:else}
        <div class="left"></div>
        <div class="inner">
            <div class="header">
                <a href="/back" on:click|preventDefault|stopPropagation={cancel}>
                    {@html chevron_left}
                </a>
                <h2>{usePublicKey ? I18n.t("webauthn.updateTitle") : I18n.t("webauthn.setTitle")}</h2>
            </div>
            <p class="info">{I18n.t("webauthn.info")}</p>
            {#if usePublicKey}
                <p class="keys">{I18n.t("webauthn.publicKeys")}</p>
                <table cellspacing="0">
                    <thead></thead>
                    <tbody>
                    {#each $user.publicKeyCredentials as credential, i}
                        <tr class="name" on:click={credentialsDetails(credential)}>
                            <td class="value" class:last={i === $user.publicKeyCredentials.length - 1}>
                                <div class="value-container">
                                    <span>{`${credential.name}`}</span>
                                    <a class="menu-link" href="/edit"
                                       on:click|preventDefault|stopPropagation={credentialsDetails(credential)}>
                                        {@html chevron_right}
                                    </a>
                                </div>
                            </td>
                        </tr>
                    {/each}
                    </tbody>
                </table>
            {:else}
                <p class="keys">{I18n.t("webauthn.noPublicKeys")}</p>

            {/if}
            <p class="info2">{I18n.t("webauthn.info2", {action: usePublicKey ? I18n.t("webauthn.updateUpdate") : I18n.t("webauthn.setUpdate")})}</p>
            <p>{I18n.t("webauthn.nameRequired")}</p>
            <label for="credentialName">{I18n.t("webauthn.credentialName")}</label>
            <input id="credentialName" type="text" placeholder={I18n.t("webauthn.credentialNamePlaceholder")}
                   bind:value={credentialName}>

            <div class="options">
                <Button className="cancel" label={I18n.t("password.cancel")} onClick={cancel}/>

                <Button label={usePublicKey ? I18n.t("webauthn.updateUpdate") : I18n.t("webauthn.setUpdate")}
                        onClick={startWebAuthn}
                        disabled={!credentialName}/>
            </div>
        </div>
    {/if}

</div>