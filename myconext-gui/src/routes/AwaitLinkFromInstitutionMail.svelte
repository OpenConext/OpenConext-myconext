<script>
    import I18n from "../locale/I18n";
    import {config, flash, user} from "../stores/user";
    import {onMount} from "svelte";
    import {createFromInstitutionVerify, resendCreateFromInstitutionMail} from "../api";
    import Modal from "../components/Modal.svelte";
    import {navigate} from "svelte-routing";
    import CodeValidation from "../components/CodeValidation.svelte";
    import mailSvg from "../icons/undraw_mailbox_e7nc.svg?raw";
    const resendMailAllowedTimeOut = $config.emailSpamThresholdSeconds * 1000;

    export let hash;

    let wrongCode = false;
    let allowedToResend = false;
    let mailHasBeenResend = false;

    onMount(() => {
        flash.setValue(I18n.t("Email.UpdatedVerified.COPY", {email: $user.email}), 6500);
        setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
    });

    const resendMail = () => {
        allowedToResend = false;
        resendCreateFromInstitutionMail(hash)
            .then(() => {
                mailHasBeenResend = true;
                setTimeout(() => {
                    allowedToResend = true;
                    mailHasBeenResend = false;
                }, resendMailAllowedTimeOut);

            }).catch(() => navigate("/create-from-institution/expired"));
    }

    const verifyCode = code => {
        createFromInstitutionVerify(hash, code)
            .then(() => {
                navigate("/security?new=true")
            }).catch(e => {
            if (e.status === 403 || e.status === 400) {
                navigate("/create-from-institution/expired");
            } else {
                wrongCode = true;
            }
        })

    }

</script>

<style lang="scss">
    .institution-mail {
        display: flex;
        min-height: 100vh;
        align-items: center;

        :global(svg) {
            margin: auto;
            width: auto;
            height: 80vh;
        }
    }

    h2.header {
        margin: 6px 0 30px 0;
        color: var(--color-primary-green);
        font-size: 28px;
    }

    p.validation-info {
        text-align: center;
        margin-bottom: 40px;
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
<div class="institution-mail">
    {@html mailSvg}
    <Modal showOptions={false}
           title={I18n.t("LoginCode.Title.COPY")}>
        <div class="login-code">
            <h2 class="header">{I18n.t("LoginCode.Header.COPY")}</h2>
            <p class="validation-info">{@html I18n.t("LoginCode.Info.COPY", {email: $user.email})}</p>
            <div class="code-validation">
                <CodeValidation verify={verifyCode}
                                size={6}
                                validate={val => !isNaN(val)}/>
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
</div>