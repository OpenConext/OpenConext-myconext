<script>
    import {config, flash, user} from "../stores/user";
    import I18n from "../locale/I18n";
    import {generateEmailChangeCode} from "../api";
    import critical from "../icons/critical.svg?raw";
    import Button from "../components/Button.svelte";
    import Modal from "../components/Modal.svelte";

    import {validEmail} from "../validation/regexp";
    import {navigate} from "svelte-routing";
    import {doLogOutAfterRateLimit} from "../utils/utils.js";
    import {resendMailChangeCode, verifyEmailChangeCode} from "../api/index.js";
    import CodeValidation from "../components/CodeValidation.svelte";

    let verifiedEmail = "";
    let duplicateEmail = false;
    let outstandingPasswordForgotten = false;

    let hasCodeValidation = false;
    let showCodeValidation = false;

    let wrongCode = false;
    let disabledButton = true;

    let allowedToResend = false;
    let mailHasBeenResend = false;

    const resendMailAllowedTimeOut = $config.emailSpamThresholdSeconds * 1000;

    const update = (force = false) => {
        if (validEmail(verifiedEmail) && verifiedEmail.toLowerCase() !== $user.email.toLowerCase()) {
            if (hasCodeValidation) {
                showCodeValidation = true;
            } else {
                generateEmailChangeCode(verifiedEmail, force)
                    .then(() => {
                        hasCodeValidation = true;
                        showCodeValidation = true;
                        flash.setValue(I18n.t("Email.Updated.COPY", {email: verifiedEmail}), 6500);
                        setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
                    }).catch(e => {
                    if (e.status === 409) {
                        duplicateEmail = true;
                    } else if (e.status === 406) {
                        outstandingPasswordForgotten = true;
                    }
                });
            }
        }
    }

    const verifyCode = code => {
        verifyEmailChangeCode(code)
            .then(res => {
                navigate(`update-email?h=${res.hash}&nav=security`)
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
        resendMailChangeCode()
            .then(() => {
                allowedToResend = false;
                setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
            }).catch(() => {
            doLogOutAfterRateLimit($config.idpBaseUrl);
        })
    }

    const valueCallback = values => {
        wrongCode = false;
        disabledButton = values.filter(v => v !== '').length !== 6;
    }

    const cancel = () => {
        history.back();
    }

    $: emailEquality = verifiedEmail.toLowerCase() === $user.email.toLowerCase();

</script>

<style lang="scss">
    .email {
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
        margin: 12px 0 32px 0;
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

        &.error {
            border: solid 1px var(--color-primary-red);
            background-color: #fff5f3;

            &:focus {
                outline: none;
            }
        }

    }

    div.error {
        display: flex;
        align-items: center;
        margin-top: 10px;

        span.error {
            color: var(--color-primary-red);
        }

        span.svg {
            display: inline-block;
            margin-right: 10px;
        }

    }

    .options {
        margin-top: 60px;
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
<div class="email">
    <h2>{I18n.t("Email.Title.Edit.COPY")}</h2>
    <p class="info">{I18n.t("email.info")}</p>
    <label for="verifiedEmail">{I18n.t("Email.Info.COPY")}</label>
    <input id="verifiedEmail"
           class:error={emailEquality || duplicateEmail}
           type="email"
           spellcheck="false"
           bind:value={verifiedEmail}/>
    {#if emailEquality}
        <div class="error">
            <span class="svg">{@html critical}</span>
            <span class="error">{I18n.t("Email.EmailEquality.COPY")}</span>
        </div>
    {/if}
    {#if duplicateEmail}
        <div class="error">
            <span class="svg">{@html critical}</span>
            <span class="error">{I18n.t("Email.DuplicateEmail.COPY")}</span>
        </div>
    {/if}
    <div class="options">
        <Button className="cancel" label={I18n.t("YourVerifiedInformation.ConfirmRemoval.Button.Cancel.COPY")}
                onClick={cancel}/>
        <Button label={I18n.t("Email.Update.COPY")} onClick={update}
                disabled={!validEmail(verifiedEmail) || emailEquality}/>
    </div>
</div>
{#if outstandingPasswordForgotten}
    <Modal submit={() => update(true)}
           cancel={() => history.back()}
           warning={true}
           question={I18n.t("Email.OutstandingPasswordForgottenConfirmation.COPY")}
           title={I18n.t("Email.OutstandingPasswordForgotten.COPY")}>
    </Modal>
{/if}

{#if showCodeValidation}
    <Modal showOptions={false}
           cancel={() => showCodeValidation = false}
           title={I18n.t("LoginCode.Title.COPY")}>
        <div class="login-code">
            <h2 class="header">{I18n.t("LoginCode.Header.COPY")}</h2>
            <p class="validation-info">{@html I18n.t("LoginCode.Info.COPY", {email: verifiedEmail})}</p>
            <div class="code-validation">
                <CodeValidation verify={verifyCode}
                                size={6}
                                validate={val => !isNaN(val)}
                                intermediateCallback={valueCallback}/>
                {#if wrongCode}
                    <p class="error">{I18n.t("LoginCode.Error.COPY")}</p>
                {/if}
            </div>

            <Button label={I18n.t("LoginCode.Continue.COPY")}
                    onClick={verifyCode}
                    fullSize={true}
                    disabled={disabledButton || wrongCode}/>

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
