<script>
    import {config, flash, user} from "../stores/user";
    import I18n from "../locale/I18n";
    import {navigate} from "svelte-routing";
    import getApp from "../icons/redesign/undraw_Mobile_app_re_catg 1.svg";
    import hasApp from "../icons/redesign/undraw_Mobile_app_small.svg";
    import Button from "../components/Button.svelte";
    import {testWebAutnUrl} from "../api";
    import {onMount} from "svelte";
    import {dateFromEpoch} from "../utils/date";
    import binIcon from "../icons/redesign/trash.svg";
    import verifiedSvg from "../icons/redesign/shield-full.svg";
    import webAuthnIcon from "../icons/redesign/video-game-key.svg";
    import passwordIcon from "../icons/redesign/password-type.svg";
    import mobilePhoneIcon from "../icons/redesign/mobile-phone.svg";
    import backupIcon from "../icons/redesign/backup-code.svg";

    import magicLinkIcon from "../icons/redesign/video-game-magic-wand.svg";
    import SecurityOption from "../components/SecurityOption.svelte";

    let usePublicKey = $user.usePublicKey;
    let showAppDetails = false;

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

    .app-details {
        display: flex;
        flex-direction: column;
        gap: 15px;
        margin-top: 20px;
        padding: 20px 0 10px 56px;
        border-top: 1px solid var(--color-tertiare-blue);

        .me {
            font-weight: 600;
        }

        .last-login {
            color: var(--color-secondary-grey);
        }

        span.de-activate {
            display: flex;
            gap: 10px;
            align-items: center;

            a {
                text-decoration: underline;
            }
        }

        :global(span.de-activate svg) {
            color: var(--color-secondary-grey);
            width: 16px;
            height: auto;
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

    }

    .recovery-options {
        padding: 25px 20px;
        background-color: var(--color-secondary-background);
        border-radius: 8px;
        display: flex;
        flex-direction: column;
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
            <SecurityOption action={() => showAppDetails = !showAppDetails}
                            icon={hasApp}
                            label={I18n.t("security.options.app")}
                            subLabel={I18n.t("security.tiqr.activated", {
                                date: dateFromEpoch($user.registration.created, false)
                            })}
                            hasSubContent={true}
                            showSubContent={showAppDetails}
                            active={true}>
                <div class="app-details">
                    <span class="me">{`${I18n.t(`security.tiqr.${$user.registration.notificationType}`)} ${$user.givenName} ${$user.familyName}`}</span>
                    <span class="last-login">{I18n.t("security.tiqr.lastLogin",
                        {date: dateFromEpoch($user.registration.updated, true)})}</span>
                    <span class="de-activate" on:click={() => navigate("/deactivate-app")}>
                        <span class="icon">{@html binIcon}</span>
                        <a href="/#"
                           on:click|preventDefault|stopPropagation={() => navigate("/deactivate-app")}>
                            {I18n.t("security.tiqr.deactivate")}
                        </a>
                    </span>
                </div>
            </SecurityOption>
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
        {#if $user.loginOptions.includes("useApp") && $user.registration && $user.registration.notificationType}
            <h4 class="info">{I18n.t("security.tiqr.backupCodes")}</h4>
            <div class="recovery-options">
                <SecurityOption action={() => navigate("/backup-codes")}
                                icon={mobilePhoneIcon}
                                label={I18n.t("security.tiqr.sms")}
                                subLabel={I18n.t(`security.tiqr.${$user.registration.recoveryCode ? "getSmsInfo" : "smsInfo"}`
                                , {phone: `** ** *** ${$user.registration.phoneNumber}`})}
                                active={!$user.registration.recoveryCode}/>

                <SecurityOption action={() => navigate("/backup-codes")}
                                icon={backupIcon}
                                label={I18n.t(`security.tiqr.${$user.registration.recoveryCode ? "code" : "getCode"}`)}
                                subLabel={I18n.t(`security.tiqr.${$user.registration.recoveryCode ? "codeInfo" : "getCodeInfo"}`)}
                                active={$user.registration.recoveryCode}/>
            </div>
        {/if}
    </div>
</div>
