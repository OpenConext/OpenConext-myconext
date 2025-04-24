<script>
    import {config, flash, user} from "../stores/user";
    import I18n from "../locale/I18n";
    import {
        generatePasswordResetCode,
        outstandingEmailLinks,
        resendPasswordChangeCode,
        verifyPasswordCode
    } from "../api";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import Modal from "../components/Modal.svelte";
    import CodeValidation from "../components/CodeValidation.svelte";
    import {doLogOutAfterRateLimit} from "../utils/utils.js";

    const usePassword = $user.usePassword;
    let outstandingEmailReset = false;
    let hasCodeValidation = false;
    let showCodeValidation = false;
    let initial = true;

    let wrongCode = false;

    let allowedToResend = false;
    let mailHasBeenResend = false;

    const resendMailAllowedTimeOut = $config.emailSpamThresholdSeconds * 1000;

    onMount(() => {
        outstandingEmailLinks().then(res => {
            outstandingEmailReset = res;
        })
    });

    const cancel = () => navigate("/security");

    const proceed = () => {
        if (initial && outstandingEmailReset) {
            initial = false;
        } else {
            if (hasCodeValidation) {
                showCodeValidation = true;
            } else {
                generatePasswordResetCode().then(() => {
                    hasCodeValidation = true;
                    showCodeValidation = true;
                    flash.setValue(I18n.t("Email.Updated.COPY", {email: $user.email}), 6500);
                    setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
                })
            }
        }
    }

    const verifyCode = code => {
        verifyPasswordCode(code)
            .then(res => {
                navigate(`reset-password?h=${res.hash}`)
            })
            .catch(e => {
                if (e.status === 403 || e.status === 400) {
                    doLogOutAfterRateLimit($config.idpBaseUrl);
                } else {
                    wrongCode = true;
                }
            })
    }

    const resendMail = () => {
        resendPasswordChangeCode()
            .then(() => {
                allowedToResend = false;
                setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
            }).catch(() => {
            doLogOutAfterRateLimit($config.idpBaseUrl);
        })
    }

    const valueCallback = values => {
        wrongCode = false;
    }

</script>

<style lang="scss">
    .password {
        width: 100%;
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    h2 {
        margin-top: 35px;
        color: var(--color-primary-green);
    }

    p.info {
        margin: 22px 0 0 0;
    }

    .options {
        margin: 60px 0 20px 0;
        display: flex;
    }

    div.login-code {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    p.validation-info {
        text-align: center;
        margin-bottom: 40px;
    }

    h2.header {
        margin: 6px 0 30px 0;
        color: var(--color-primary-green);
        font-size: 28px;
    }

    div.code-validation {
        margin-bottom: 40px;
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    p.error {
        margin-top: 10px;
        color: var(--color-primary-red);
    }

    div.resend-mail {
        margin-top: 30px;
        font-size: 15px;
        text-align: center;
    }


</style>
<div class="password">
    <h2>{usePassword ? I18n.t("ChangePassword.Title.ChangePassword.COPY") : I18n.t("PasswordResetLink.Title.AddPassword.COPY")}</h2>
    <p class="info">{@html usePassword ? I18n.t("Password.UpdateInfo.COPY") : I18n.t("Password.AddInfo.COPY")}</p>
    <div class="options">
        <Button className="cancel" label={I18n.t("YourVerifiedInformation.ConfirmRemoval.Button.Cancel.COPY")}
                onClick={cancel}/>

        <Button label={I18n.t("ConfirmDelete.Button.Confirm.COPY")}
                onClick={proceed}/>
    </div>
</div>
{#if outstandingEmailReset && !initial}
    <Modal submit={proceed}
           cancel={() => navigate("/security")}
           warning={true}
           question={I18n.t("Password.OutstandingEmailResetConfirmation.COPY")}
           title={I18n.t("Password.OutstandingEmailReset.COPY")}>
    </Modal>
{/if}

{#if showCodeValidation}
    <Modal showOptions={false}
           cancel={() => showCodeValidation = false}
           title={I18n.t("LoginCode.Title.COPY")}>
        <div class="login-code">
            <h2 class="header">{I18n.t("LoginCode.Header.COPY")}</h2>
            <p class="validation-info">{@html I18n.t("LoginCode.Info.COPY", {email: $user.email})}</p>
            <div class="code-validation">
                <CodeValidation verify={verifyCode}
                                size={6}
                                validate={val => !isNaN(val)}
                                intermediateCallback={valueCallback}/>
                {#if wrongCode}
                    <p class="error">{I18n.t("LoginCode.Error.COPY")}</p>
                {/if}
            </div>

            <div class="resend-mail">
                {#if allowedToResend}
                    <p>{I18n.t("LoginCode.Resend.COPY")}
                        <a href="resend"
                           on:click|preventDefault|stopPropagation={resendMail}>{I18n.t("LoginCode.ResendLink.COPY")}</a>
                    </p>
                {:else if mailHasBeenResend}
                    <span>{I18n.t("MagicLink.MailResend.COPY")}</span>
                {/if}

            </div>
        </div>

    </Modal>
{/if}