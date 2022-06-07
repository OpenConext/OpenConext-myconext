<script>
    import {config, flash, user} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import writeSvg from "../icons/redesign/pencil-write.svg";
    import getApp from "../icons/redesign/undraw_Mobile_app_re_catg 1.svg";
    import hashApp from "../icons/redesign/undraw_Order_confirmed_re_g0if.svg";
    import rocketSvg from "../icons/redesign/space-rocket-flying.svg";
    import {supported} from "@github/webauthn-json"
    import Button from "../components/Button.svelte";
    import {forgetMe, testWebAutnUrl} from "../api";
    import verifiedSvg from "../icons/redesign/remembered.svg";
    import nonVerifiedSvg from "../icons/redesign/not-remembered.svg";
    import Modal from "../components/Modal.svelte";
    import {onMount} from "svelte";
    import {dateFromEpoch} from "../utils/date";

    let password = $user.usePassword ? "************************" : I18n.t("security.notSet");
    let passwordStyle = $user.usePassword ? "value" : "value-alt";

    const supportsWebAuthn = supported();
    let publicKey = $user.usePublicKey ? "************************" :
        supportsWebAuthn ? I18n.t("security.notSet") : I18n.t("security.notSupported");

    let publicKeyStyle = $user.usePublicKey ? "value" : "value-alt";
    let usePublicKey = $user.usePublicKey;

    let showModal = false;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const testWebAuthn = urlSearchParams.get("success");
        if (testWebAuthn) {
            flash.setValue(I18n.t("webauthn.testFlash"), 3750);
        }
    });

    const doForgetMe = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true
        } else {
            forgetMe().then(() => {
                $user.rememberMe = false;
                showModal = false;
                flash.setValue(I18n.t("rememberMe.updated"));
            });
        }
    };

    const credentialsDetails = credential => () =>
        navigate(`/credential?id=${encodeURIComponent(credential.identifier)}`);

    const startTestFlow = () => {
        testWebAutnUrl().then(res => {
            window.location.href = res.url;
        });
    }

</script>

<style lang="scss">
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
    }

    p {
        line-height: 1.33;
        letter-spacing: normal;
    }

    table {
        width: 100%;
        margin-bottom: 30px;

        &.no-bottom-margin {
            margin-bottom:0 ;
        }
        tr.link {
            cursor: pointer;

            &:hover {
                background-color: var(--color-background);
            }

        }

        td {
            border-bottom: 1px solid var(--color-primary-grey);

            &.last {
                border-bottom: none;
            }

            &.space {
                padding: 5px 0;
            }

        }

        td.attr {
            width: 40%;
            padding: 20px;
        }

        td.value {
            width: 60%;
            font-weight: bold;
        }

        div.value-container {
            display: flex;
            align-items: center;

            span {
                word-break: break-word;
            }

            a.right-link {
                margin-left: auto;
            }

            div.actions {
                display: flex;
                margin-left: auto;
                align-items: center;

                a.right-link {
                    margin-left: 20px;
                }

            }
        }

        @media (max-width: 820px) {
            div.value-container {
                flex-direction: column;
                align-items: flex-start;

                div.actions {
                    margin-left: 0;
                    width: 100%;

                    a.right-link {
                        margin-left: auto;
                    }

                }
            }
        }

        td.value-alt {
            width: 65%;
            font-style: italic;
            color: #797979;

        }

        td.link {
            width: 10%;
            text-align: right;
            padding: 0;
        }

    }

    h4.last {
        margin-top: 25px;
    }

    table.security-settings {
        td {
            border-bottom: none;

            &.attr {
                width: 28%;
            }

            &.icon {
                width: 70px;
                text-align: center;
            }

            &.info {
                width: 80%;

                &.verified {
                    width: 56%;
                }
            }

            &.forget-me {
                width: 122px;
                text-align: right;
            }

        }
    }

    :global(div.value-container a.right-link svg) {
        color: var(--color-secondary-grey);
        width: 22px;
        height: auto;
    }

    .tiqr-app {
        background-color: white;
        display: flex;
        padding: 15px;
        border: 1px solid var(--color-secondary-grey);
        margin-bottom: 30px;
        border-radius: 8px;

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
    }

</style>
<div class="security">
    <div class="inner-container">
        <h2>{I18n.t("security.title")}</h2>
        <p class="info">{I18n.t("security.subTitle")}</p>
        <div class="tiqr-app">
            {#if $user.loginOptions.includes("useApp") }
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
                            <td class="attr">{I18n.t("security.tiqr.lastLogin")}</td>
                            <td class="value">{dateFromEpoch($user.registration.updated, true)}</td>
                        </tr>
                        <tr>
                            <td class="attr last">{I18n.t("security.tiqr.activated")}</td>
                            <td class="value last ">{dateFromEpoch($user.registration.updated, false)}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                <div class="has-app-image">
                    {@html hashApp}
                    <Button label={I18n.t("security.tiqr.deactivate")}
                            medium={true}
                            onClick={() => navigate("/deactivate-app")}/>
                </div>
            {:else}
                <div class="information">
                    <h4>{I18n.t("security.tiqr.title")}</h4>
                    <p>{@html I18n.t("security.tiqr.info")}</p>
                    <Button label={I18n.t("security.tiqr.fetch")} large={true} onClick={() => navigate("/get-app")}/>
                </div>
                <div class="image">
                    {@html getApp}
                </div>
            {/if}
        </div>
        <h4 class="info2">{I18n.t("security.secondSubTitle")}</h4>

        <table cellspacing="0">
            <thead></thead>
            <tbody>
            <tr class="link" on:click={() => navigate("/edit-email?back=security")}>
                <td class="attr">{I18n.t("security.useMagicLink")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{$user.email}</span>
                        <a class="right-link" href="/edit-email"
                           on:click|preventDefault|stopPropagation={() => navigate("/edit-email?back=security")}>{@html writeSvg}</a>
                    </div>
                </td>
            </tr>
            <tr class="link" on:click={() => navigate("/password")}>
                <td class="attr">{I18n.t("security.usePassword")}</td>
                <td class="{passwordStyle}">
                    <div class="value-container">
                        <span>{password}</span>
                        <a class="right-link" href="/password"
                           on:click|preventDefault|stopPropagation={() => navigate("/password")}>{@html writeSvg}</a>
                    </div>
                </td>
            </tr>
            {#if $config.featureWebAuthn && usePublicKey}
                {#each $user.publicKeyCredentials as credential, i}
                    <tr class="link" on:click={credentialsDetails(credential)}>
                        <td class="attr">{I18n.t("security.securityKey", {nbr: i + 1})}</td>
                        <td class="value">
                            <div class="value-container">
                                <span>{`${credential.name}`}</span>
                                <div class="actions">
                                    <Button small={true}
                                            inline={true}
                                            label={I18n.t("security.test")}
                                            icon={rocketSvg}
                                            onClick={startTestFlow}/>
                                    <a class="right-link" href="/edit"
                                       on:click|preventDefault|stopPropagation={credentialsDetails(credential)}>
                                        {@html writeSvg}
                                    </a>
                                </div>
                            </div>
                        </td>
                    </tr>
                {/each}
            {/if}
            {#if $config.featureWebAuthn }
                <tr>
                    <td class="attr last">
                        <Button label={I18n.t("security.addSecurityKey")}
                                onClick={() => navigate("/webauthn")}/>
                    </td>
                    <td class="last space">{I18n.t("security.addSecurityKeyInfo")}</td>
                </tr>
            {/if}
            </tbody>
        </table>
        <h4 class="info2 last">{I18n.t("security.settings")}</h4>
        <table class="security-settings" cellspacing="0">
            <thead></thead>
            <tbody>
            <tr>
                <td class="attr">{I18n.t("security.rememberMe")}</td>
                <td class="icon">{@html $user.rememberMe ? verifiedSvg : nonVerifiedSvg}</td>
                <td class="info" class:verified={$user.rememberMe}>
                    <span>{@html $user.rememberMe ? I18n.t("security.rememberMeInfo") : I18n.t("security.noRememberMeInfo")}</span>
                </td>
                {#if $user.rememberMe}
                    <td class="forget-me">
                        <Button label={I18n.t("rememberMe.update")}
                                onClick={doForgetMe(true)}/>
                    </td>
                {/if}
            </tr>
            </tbody>
        </table>
    </div>

</div>

{#if showModal}
    <Modal submit={doForgetMe(false)}
           cancel={() => showModal = false}
           question={I18n.t("rememberMe.forgetMeConfirmation")}
           title={I18n.t("rememberMe.forgetMe")}>

    </Modal>
{/if}