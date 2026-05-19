<script>
    import {config, flash, user} from "../stores/user";
    import I18n from "../locale/I18n";
    import {navigate} from "svelte-routing";
    import getApp from "../icons/redesign/undraw_Mobile_app_re_catg 1.svg?raw";
    import hasApp from "../icons/redesign/undraw_Mobile_app_small.svg?raw";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import {dateFromEpoch} from "../utils/date";
    import binIcon from "../icons/redesign/trash.svg?raw";
    import verifiedSvg from "../icons/redesign/shield-full.svg?raw";
    import webAuthnIcon from "../icons/redesign/video-game-key.svg?raw";
    import passwordIcon from "../icons/redesign/password-type.svg?raw";
    import mobilePhoneIcon from "../icons/redesign/mobile-phone.svg?raw";
    import backupIcon from "../icons/redesign/backup-code.svg?raw";
    import codeIcon from "../icons/mail.svg?raw";
    import SecurityOption from "../components/SecurityOption.svelte";

    let usePublicKey = $user.usePublicKey;
    let showAppDetails = false;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const newUser = urlSearchParams.get("new");
        if (newUser) {
            flash.setValue(I18n.t(`CreateFromInstitution.Welcome.COPY${newUser === "false" ? "Existing" : ""}`), 3750);
        }

    });

    const credentialsDetails = credential => () =>
        navigate(`/credential?id=${encodeURIComponent(credential.identifier)}`);

    const shouldShowAppOptions = $config.useApp
    const userHasActiveApp = $user.loginOptions.includes("useApp")
    const userIsFullyEnrolledWithApp = userHasActiveApp && $user.registration?.notificationType

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

    h3 {
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

            h3 {
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

    .security-options-group {
        display: flex;
        flex-direction: column;
        gap: 20px;
        margin-bottom: 30px;

        &.recovery-options {
            padding: 25px 20px;
            background-color: var(--color-secondary-background);
            border-radius: 8px;
        }
    }
</style>
<div class="security">
    <div class="inner-container">
        <h2>{I18n.t("Security.Title.COPY")}</h2>
        <p class="info">{I18n.t("security.subTitle")}</p>

        <!-- Banner -->
        {#if shouldShowAppOptions && !userIsFullyEnrolledWithApp }
            <div class="banner">
                <span class="verified-badge">{@html verifiedSvg}</span>
                <p class="banner-info">{I18n.t("security.banner")}</p>
            </div>
        {/if}

        <!-- Current sign in options -->
        <h3 class="info">{I18n.t("security.currentSignInOptions")}</h3>
        <div class="security-options-group">
            {#if shouldShowAppOptions && userIsFullyEnrolledWithApp}
                <SecurityOption action={() => showAppDetails = !showAppDetails}
                                icon={hasApp}
                                label={I18n.t("security.options.app")}
                                subLabel={I18n.t("Security.Tiqr.Activated.COPY", {
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
                            icon={codeIcon}
                            label={I18n.t("Security.UseCode.COPY")}
                            subLabel={$user.email}
                            active={true}/>
            {#if $user.usePassword}
                <SecurityOption action={() => navigate("/reset-password-link")}
                                icon={passwordIcon}
                                label={I18n.t("Security.ChangePassword.COPY")}
                                subLabel={I18n.t("Security.PasswordActivated.COPY", {
                                    date: $user.passwordUpdatedAt ? dateFromEpoch($user.passwordUpdatedAt, false) : ''
                                })}
                                active={true}/>
            {/if}
            {#if $config.featureWebAuthn && usePublicKey}
                {#each $user.publicKeyCredentials as credential, i}
                    <SecurityOption action={credentialsDetails(credential)}
                                    icon={webAuthnIcon}
                                    label={I18n.t("security.options.passkey")}
                                    subLabel={I18n.t("Security.CredentialActivated.COPY", {
                                        name: credential.name,
                                        date: dateFromEpoch(credential.createdAt, false)
                                    })}
                                    active={true}/>
                {/each}
            {/if}
        </div>

        <!-- Recommended methods -->
        {#if shouldShowAppOptions && !userIsFullyEnrolledWithApp}
            <h3 class="info">{I18n.t("security.recommendedOptions")}</h3>
            <div class="tiqr-app">
                <div class="information">
                    <h3 class="grey">{I18n.t("Security.Tiqr.Title.COPY")}</h3>
                    <p>{@html I18n.t("Security.Tiqr.Info.COPY")}</p>
                    <Button label={I18n.t("Security.Tiqr.Fetch.COPY")} large={true} onClick={() => navigate("/get-app")}/>
                </div>
                <div class="image">
                    {@html getApp}
                </div>
            </div>
        {/if}

        <!-- Other methods -->
        <h3 class="info">{I18n.t("Security.OtherMethods.COPY")}</h3>
        <div class="security-options-group">
            {#if !$user.usePassword}
                <SecurityOption action={() => navigate("/reset-password-link")}
                                icon={passwordIcon}
                                label={I18n.t("Security.AddPassword.COPY")}
                                active={false}/>
            {/if}
            {#if $config.featureWebAuthn }
                <SecurityOption action={() => navigate("/webauthn")}
                                icon={webAuthnIcon}
                                label={I18n.t("security.options.passkeyAdd")}
                                    active={false}/>
            {/if}
        </div>

        <!-- Recovery Options (Mobile App) -->
        {#if shouldShowAppOptions && userIsFullyEnrolledWithApp}
            <h3 class="info">{I18n.t("security.tiqr.backupCodes")}</h3>
            <div class="security-options-group recovery-options">
                {#if $user.registration.recoveryCode}
                    <SecurityOption action={() => navigate("/backup-codes")}
                                    icon={backupIcon}
                                    label={I18n.t(`security.tiqr.${$user.registration.recoveryCode ? "code" : "getCode"}`)}
                                    subLabel={I18n.t(`security.tiqr.${$user.registration.recoveryCode ? "codeInfo" : "getCodeInfo"}`)}
                                    active={true}/>
                {:else}
                    <SecurityOption action={() => navigate("/backup-codes")}
                                    icon={mobilePhoneIcon}
                                    label={I18n.t("Security.Tiqr.Sms.COPY")}
                                    subLabel={I18n.t(`security.tiqr.${$user.registration.recoveryCode ? "getSmsInfo" : "smsInfo"}`
                                    , {phone: `** ** *** ${$user.registration.phoneNumber}`})}
                                    active={true}/>
                {/if}

            </div>
        {/if}
    </div>
</div>
