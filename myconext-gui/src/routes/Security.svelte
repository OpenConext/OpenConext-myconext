<script>
    import {config, flash, user} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import getApp from "../icons/redesign/undraw_Mobile_app_re_catg 1.svg";
    import hasApp from "../icons/redesign/undraw_Mobile_app_small.svg";
    import hashApp from "../icons/redesign/undraw_Order_confirmed_re_g0if.svg";
    import Button from "../components/Button.svelte";
    import {testWebAutnUrl} from "../api";
    import {onMount} from "svelte";
    import {dateFromEpoch} from "../utils/date";
    import verifiedSvg from "../icons/redesign/shield-full.svg";
    import webAuthnIcon from "../icons/redesign/video-game-key.svg";
    import passwordIcon from "../icons/redesign/password-type.svg";

    import magicLinkIcon from "../icons/redesign/video-game-magic-wand.svg";
    import SecurityOption from "../components/SecurityOption.svelte";
    import rocketSvg from "../icons/redesign/space-rocket-flying.svg";
    import writeSvg from "../icons/redesign/pencil-write.svg";

    let usePublicKey = $user.usePublicKey;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const newUser = urlSearchParams.get("new");
        if (newUser) {
            flash.setValue(I18n.t(`createFromInstitution.welcome${newUser === "false" ? "Existing" : ""}`), 3750);
        }

    });

    const credentialsDetails = credential => () =>
        navigate(`/credential?id=${encodeURIComponent(credential.identifier)}`);

    const startTestFlow = () => {
        testWebAutnUrl().then(res => {
            window.location.href = res.url;
        });
    }

</script>

<style lang="scss">

    $max-width-mobile: 1080px;
    $max-width-not-edit: 480px;

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

    h4 {
        margin-bottom: 2px;

        &.info {
            margin-bottom: 20px;
        }
    }

    p {
        line-height: 1.33;
        letter-spacing: normal;
    }

    table {
        width: 100%;
        margin-bottom: 30px;

        &.no-bottom-margin {
            margin-bottom: 0;
        }

        td {
            border-bottom: 1px solid var(--color-primary-grey);

            &.last {
                border-bottom: none;
            }

        }

        td.attr {
            width: 43%;
            padding: 20px 25px 20px 10px;
        }

        td.value {
            width: 57%;
            font-weight: bold;
        }


    }

    div.banner {
        display: flex;
        align-items: center;
        background-color: var(--color-secondary-blue);
        padding: 10px;
        margin: 14px 0 40px 0;

        @media (max-width: $max-width-mobile) {
            flex-direction: column;
            gap: 15px;
            align-items: flex-start;
            margin-top: 20px;
            padding: 10px;
        }

        span.verified-badge {
            margin-left: 5px;
            @media (max-width: $max-width-mobile) {
                margin-left: 0;
            }

            :global(svg) {
                height: 28px;
                width: auto;
            }
        }

        p.banner-info {
            margin: 0 20px;
            font-weight: 600;
            @media (max-width: $max-width-mobile) {
                margin: 0;
            }
        }
    }

    .tiqr-app {
        background-color: white;
        display: flex;
        padding: 15px;
        border: 1px solid var(--color-secondary-grey);
        margin-bottom: 30px;
        border-radius: 8px;
        color: var(--color-secondary-grey);

        @media (max-width: 820px) {
            flex-direction: column;
        }

        .information {
            display: flex;
            flex-direction: column;

            h4 {
                margin-bottom: 25px;
            }

            p {
                margin-bottom: 20px;
            }
        }

        :global(.information a) {
            margin-top: auto;
        }

        :global(.image svg) {
            width: 210px;
            margin-left: 20px;
            height: auto;
        }

        .has-app-image {
            display: flex;
            flex-direction: column;
        }

        :global(.has-app-image svg) {
            width: 210px;
            margin: 20px;
            height: auto;
        }

        :global(.has-app-image a) {
            margin-top: auto;
            margin-left: auto;
        }

        :global(.has-app-image a.down) {
            margin-top: 20px;
        }
    }

</style>
<div class="security">
    <div class="inner-container">
        <h2>{I18n.t("security.title")}</h2>
        <p class="info">{I18n.t("security.subTitle")}</p>
        {#if !$user.loginOptions.includes("useApp")}
            <div class="banner">
                <span class="verified-badge">{@html verifiedSvg}</span>
                <p class="banner-info">{I18n.t("security.banner")}</p>
            </div>
        {/if}

        <h4 class="info">{I18n.t("security.currentSignInOptions")}</h4>

        {#if $user.loginOptions.includes("useApp") && $user.registration && $user.registration.notificationType}
            <div class="tiqr-app">
                <div class="information">
                    <h4>{I18n.t("security.tiqr.app")}</h4>

                    <table cellspacing="0" class="no-bottom-margin">
                        <thead></thead>
                        <tbody>
                        <tr>
                            <td class="attr">{I18n.t("security.tiqr.phoneId")}</td>
                            <td class="value">
                                {`${I18n.t(`security.tiqr.${$user.registration.notificationType}`)} ${$user.givenName} ${$user.familyName}`}
                            </td>
                        </tr>
                        <tr>
                            <td class="attr">{I18n.t("security.tiqr.backupMethod")}</td>
                            <td class="value">{I18n.t(`security.tiqr.${$user.registration.recoveryCode ? "code" : "sms"}`)}</td>
                        </tr>
                        <tr>
                            <td class="attr">{I18n.t("security.tiqr.lastLogin")}</td>
                            <td class="value">{dateFromEpoch($user.registration.updated, true)}</td>
                        </tr>
                        <tr>
                            <td class="attr last">{I18n.t("security.tiqr.activated")}</td>
                            <td class="value last ">{dateFromEpoch($user.registration.created, false)}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                <div class="has-app-image">
                    {@html hashApp}
                    <Button label={I18n.t("security.tiqr.deactivate")}
                            large={true}
                            onClick={() => navigate("/deactivate-app")}/>
                    <Button label={I18n.t("security.tiqr.backupCodes")}
                            large={true}
                            className="down"
                            onClick={() => navigate("/backup-codes")}/>
                </div>
            </div>

        {/if}
        <SecurityOption action={() => navigate("/edit-email")}
                        icon={magicLinkIcon}
                        label={I18n.t("security.magicLinkOption")}
                        subLabel={$user.email}
                        active={true}/>

        {#if $user.usePassword}
            <SecurityOption action={() => navigate("/reset-password-link")}
                            icon={passwordIcon}
                            label={I18n.t("security.options.password")}
                            subLabel="*****************"
                            active={true}/>
        {/if}

        {#if $config.featureWebAuthn && usePublicKey}
            {#each $user.publicKeyCredentials as credential, i}
                <SecurityOption action={credentialsDetails(credential)}
                                icon={webAuthnIcon}
                                label={I18n.t("security.options.passkey")}
                                subLabel={credential.name}
                                active={true}/>
            {/each}
        {/if}

        {#if !$user.loginOptions.includes("useApp")}
            <h4 class="info">{I18n.t("security.recommendedOptions")}</h4>
            <div class="tiqr-app">
                <div class="information">
                    <h4 class="grey">{I18n.t("security.tiqr.title")}</h4>
                    <p>{@html I18n.t("security.tiqr.info")}</p>
                    <Button label={I18n.t("security.tiqr.fetch")} large={true} onClick={() => navigate("/get-app")}/>
                </div>
                <div class="image">
                    {@html getApp}
                </div>
            </div>
            {/if}

        <h4 class="info">{I18n.t("security.otherMethods")}</h4>
        {#if !$user.usePassword}
            <SecurityOption action={() => navigate("/reset-password-link")}
                            icon={passwordIcon}
                            label={I18n.t("security.options.passwordAdd")}
                            active={false}/>
        {/if}
            {#if $config.featureWebAuthn }
                <SecurityOption action={() => navigate("/webauthn")}
                                icon={webAuthnIcon}
                                label={I18n.t("security.options.passkeyAdd")}
                                active={false}/>
            {/if}


    </div>
</div>
