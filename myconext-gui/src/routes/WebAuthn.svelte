<script>
    import {config, user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {startWebAuthFlow} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";
    import Spinner from "../components/Spinner.svelte";

    let usePublicKey = $user.usePublicKey;
    let loading = false;

    const startWebAuthn = () => {
        loading = true;
        startWebAuthFlow().then(res => {
            window.location.href = `${$config.eduIDWebAuthnUrl}?token=${res.token}`
        });
    }

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
        margin-top: 32px;
    }

    p.info2 {
        margin-top: 22px;
        margin-bottom: 32px;
    }

    p.info3 {
        margin-top: 22px;
        margin-bottom: 32px;
    }

    .options {
        margin-top: 60px;
    }

    :global(.options a:not(:first-child)) {
        margin-left: 25px;
    }


</style>
<div class="web-authn">
    <div class="left"></div>
    <div class="inner">
        <div class="header">
            <a href="/back" on:click|preventDefault|stopPropagation={cancel}>
                {@html chevron_left}
            </a>
            <h2>{usePublicKey ? I18n.t("webauthn.updateTitle") : I18n.t("webauthn.setTitle")}</h2>
        </div>
        <p class="info">{I18n.t("webauthn.info")}</p>
        <p class="info2">{I18n.t("webauthn.info2", {action: usePublicKey ? I18n.t("webauthn.updateUpdate") : I18n.t("webauthn.setUpdate")})}</p>
        {#if loading}
            <Spinner/>
        {/if}
        {#if usePublicKey}
                <p class="info3">{I18n.t("webauthn.currentKeys", {count: Object.keys($user.publicKeyCredentials).length})}</p>
        {/if}
        <div class="options">
            <Button className="cancel" label={I18n.t("password.cancel")} onClick={cancel}/>

            <Button label={usePublicKey ? I18n.t("webauthn.updateUpdate") : I18n.t("webauthn.setUpdate")}
                    onClick={startWebAuthn}
                    disabled={false}/>
        </div>
    </div>


</div>