<script>
    import I18n from "../locale/I18n";
    import {user} from "../stores/user";
    import {onMount} from "svelte";
    import {fetchServiceName, resendCodeMail, verifyCodeExistingUser} from "../api";
    import {conf, links} from "../stores/conf";
    import CodeValidation from "../components/CodeValidation.svelte";
    import {navigate} from "svelte-routing";
    import Cookies from "js-cookie";
    import {cookieNames} from "../constants/cookieNames.js";
    import {loginPreferences} from "../constants/loginPreferences.js";
    import Spinner from "../components/Spinner.svelte";
    import Button from "../components/Button.svelte";

    const resendMailAllowedTimeOut = $conf.emailSpamThresholdSeconds * 1000;

    export let id;

    let showSpinner = false;
    let allowedToResend = false;
    let mailHasBeenResend = false;
    let wrongCode = false;
    let disabledButton = true;
    let delay = 1;

    onMount(() => {
        $links.displayBackArrow = false;
        setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
    });

    const verifyCode = code => {
        if (disabledButton) {
            return;
        }
        showSpinner = true;
        verifyCodeExistingUser(code, id)
            .then(json => {
                showSpinner = false;
                Cookies.set(cookieNames.LOGIN_PREFERENCE, loginPreferences.CODE, {
                    expires: 365,
                    secure: true,
                    sameSite: "Lax"
                });
                if (json.stepup) {
                    fetchServiceName(id).then(serviceName => {
                        navigate(`/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${json.explanation}`, {replace: true})
                    })
                } else {
                    window.location.href = json.url;
                }
            })
            .catch(e => {
                showSpinner = false;
                if (e.status === 403) {
                    navigate("/rate-limited")
                } else {
                    wrongCode = true;
                    delay = delay * 2;
                    setTimeout(() => {
                        disabledButton = false;
                    }, delay * 1000)
                }
            });
    }

    const valueCallback = values => {
        wrongCode = false;
        disabledButton = values.filter(v => v !== '').length !== 6;
    }

    const resendMail = () => {
        allowedToResend = false;
        resendCodeMail(id).then(() => {
            mailHasBeenResend = true;
            setTimeout(() => {
                allowedToResend = true;
                mailHasBeenResend = false;
            }, resendMailAllowedTimeOut);

        });
    }


</script>

<style lang="scss">

    div.login-code {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    p.info {
        text-align: center;
        margin-bottom: 40px;
    }

    h2.header {
        margin: 6px 0 30px 0;
        color: var(--color-primary-green);
        font-size: 28px;

        &.error {
            color: var(--color-primary-red);
        }
    }

    div.code-validation {
        margin-bottom: 40px;
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    div.resend-mail {
        margin-top: 30px;
        font-size: 15px;
        text-align: center;
    }

    p.error {
        margin-top: 10px;
        color: var(--color-primary-red);
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<div class="login-code">
        <h2 class="header">{I18n.t("LoginCode.Header.COPY")}</h2>
        <p class="info">{@html I18n.t("LoginCode.Info.COPY", {email: $user.email})}</p>
        <div class="code-validation">
            <CodeValidation verify={verifyCode}
                            size={6}
                            validate={val => !isNaN(val)}
                            intermediateCallback={valueCallback}/>
            {#if wrongCode}
                <p class="error">{I18n.t("LoginCode.Error.COPY", {delay: delay})}</p>
            {/if}
        </div>

        <Button label={I18n.t("LoginCode.Continue.COPY")}
                onClick={verifyCode}
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
