<script>
    import {user, flash, config} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import chevron_right from "../icons/chevron-right.svg";
    import CheckBox from "../components/CheckBox.svelte";
    import {supported} from "@github/webauthn-json"

    let password = $user.usePassword ? "************************" : I18n.t("security.notSet");
    let passwordStyle = $user.usePassword ? "value" : "value-alt";

    const supportsWebAuthn = supported();
    let publicKey = $user.usePublicKey ? "************************" :
            supportsWebAuthn ? I18n.t("security.notSet") : I18n.t("security.notSupported");

    let publicKeyStyle = $user.usePublicKey ? "value" : "value-alt";

</script>

<style>
    .security {
        width: 100%;
        height: 100%;
    }

    .inner-container {
        height: 100%;
        margin: 0 auto;
        padding: 15px 30px 15px 0;
        display: flex;
        flex-direction: column;
    }

    h2 {
        margin: 20px 0 10px 0;
        color: var(--color-primary-green);
    }

    p.info {
        font-weight: 300;
        margin-bottom: 26px;
    }

    p.info2 {
        font-size: 22px;
        margin-bottom: 24px;
        font-family: Nunito, sans-serif;
    }

    p {
        font-size: 18px;
        line-height: 1.33;
        letter-spacing: normal;
    }

    table {
        width: 100%;
    }

    tr.name, tr.rememberme {
        cursor: pointer;
    }

    td {
        border-bottom: 1px solid var(--color-primary-grey);
    }

    td.attr {
        width: 30%;
        padding: 20px;
    }

    td.value {
        width: 60%;
        font-weight: bold;
    }

    td.value {
        width: 70%;
        font-weight: bold;
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

    td.value-alt {
        width: 70%;
        font-style: italic;
        color: #797979;
    }

    td.link {
        width: 10%;
        text-align: right;
        padding: 0;
    }

    :global(a svg.menu-link) {
        fill: var(--color-primary-green);
    }


</style>
<div class="security">
    <div class="inner-container">
        <h2>{I18n.t("security.title")}</h2>
        <p class="info">{I18n.t("security.subTitle")}</p>
        <p class="info2">{I18n.t("security.secondSubTitle")}</p>

        <table cellspacing="0">
            <thead></thead>
            <tbody>
            <tr>
                <td class="attr">{I18n.t("security.useMagicLink")}</td>
                <td class="value">{$user.email}</td>
            </tr>
            <tr class="name" on:click={() => navigate("/password")}>
                <td class="attr">{I18n.t("security.usePassword")}</td>
                <td class="{passwordStyle}">
                    <div class="value-container">
                        <span>{password}</span>
                        <a class="menu-link" href="/password"
                           on:click|preventDefault|stopPropagation={() => navigate("/password")}>{@html chevron_right}</a>
                    </div>
                </td>
            </tr>
            {#if $config.featureWebAuthn }
                <tr class:name={supportsWebAuthn} on:click={() => supportsWebAuthn && navigate("/webauthn")}>
                    <td class="attr">{I18n.t("security.usePublicKey")}</td>
                    <td class="{publicKeyStyle}">
                        <div class="value-container">
                            <span>{publicKey}</span>
                            {#if supportsWebAuthn}
                                <a class="menu-link" href="/webauthn"
                                   on:click|preventDefault|stopPropagation={() => supported() && navigate("/webauthn")}>{@html chevron_right}</a>
                            {/if}
                        </div>
                    </td>
                </tr>
            {/if}
            <tr class:rememberme={$user.rememberMe} on:click={() => $user.rememberMe && navigate("/rememberme")}>
                <td class="attr">{I18n.t("security.rememberMe")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{I18n.t(`security.rememberMe${$user.rememberMe}`)}</span>
                        {#if $user.rememberMe}
                            <a class="menu-link" href="/rememberme"
                               on:click|preventDefault|stopPropagation={() => navigate("/rememberme")}>{@html chevron_right}</a>
                        {/if}
                    </div>
                </td>
            </tr>
            </tbody>
        </table>

    </div>

</div>