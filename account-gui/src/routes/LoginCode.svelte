<script>
    import I18n from "../locale/I18n";
    import {user} from "../stores/user";
    import {onMount} from "svelte";
    import {codeExistingUser, generateCodeExistingUser, resendCodeMail} from "../api";
    import {conf, links} from "../stores/conf";
    import CodeValidation from "../components/CodeValidation.svelte";
    import {isEmpty} from "../utils/utils.js";
    import {navigate} from "svelte-routing";
    import Cookies from "js-cookie";
    import {cookieNames} from "../constants/cookieNames.js";
    import {loginPreferences} from "../constants/loginPreferences.js";
    import Spinner from "../components/Spinner.svelte";
    import Button from "../components/Button.svelte";

    const resendMailAllowedTimeOut = $conf.emailSpamThresholdSeconds * 1000;

    export let id;

    let showSpinner = true;
    let allowedToResend = false;
    let mailHasBeenResend = false;
    let wrongCode = false;

    onMount(() => {
        $links.displayBackArrow = false;
        generateCodeExistingUser($user.email, id)
            .then(() => {
                showSpinner = false;
                setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
            })
            .catch(() => navigate("/expired", {replace: true}));
    });

    const verifyCode = code => {
        showSpinner = true;
        codeExistingUser($user.email, code, id)
            .then(json => {
                Cookies.set(cookieNames.LOGIN_PREFERENCE, loginPreferences.CODE, {
                    expires: 365,
                    secure: true,
                    sameSite: "Lax"
                });
                if (json.stepup) {
                    navigate(`/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${json.explanation}`, {replace: true})
                } else {
                    window.location.href = json.url;
                }
            })
            .catch(() => {
                wrongCode = true;
            });
    }

    const valueCallback = values => {
        wrongCode = isEmpty(values) ? wrongCode : false;
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
        margin-top: 40px;
        display: flex;
        flex-direction: column;
        align-items: center;
        align-content: center;

        &.no-center {
            align-items: normal;
            align-content: normal;
        }
    }

    p {
        text-align: center;

        &.no-center {
            text-align: left;
            margin-bottom: 15px;
        }
    }

    h2.header {
        margin: 6px 0 30px 0;
        color: var(--color-primary-green);
        font-size: 28px;
    }

    div.mail-clients {
        width: 100%;
        display: flex;
        margin-top: 45px;
        align-items: center;
        align-content: center;
    }

    div.spinner-container {
        margin-top: 35px;
        display: flex;
        flex-direction: column;
        align-items: center;
        align-content: center;
    }

    div.spinner-container p {
        margin-top: 15px;
    }

    div.mail-client {
        display: flex;
        align-items: center;
        align-content: center;
        font-size: 15px;
    }

    div.mail-client img {
        display: inline-block;
        margin-right: 7px;
    }

    div.mail-client.gmail {
        margin-right: auto;
        cursor: pointer;
    }

    div.mail-client.outlook {
        margin-left: auto;
    }

    div.mail-clients a {
        text-decoration: none;
        color: #606060;
    }

    div.mail-clients a:hover {
        text-decoration: underline;
        color: var(--color-primary-blue);
    }

    div.spam, div.resend-mail {
        margin-top: 30px;
        font-size: 15px;
        text-align: center;
    }

    div.error {
        display: flex;
        align-items: center;
        color: var(--color-primary-red);
        margin-bottom: 25px;
    }

    div.error span.svg {
        display: inline-block;
        margin-right: 10px;
    }

    span.email {
        display: inline-block;
        margin-left: 4px;
        font-weight: 600;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<div class="login-code">
    <h2 class="header">{I18n.t("LoginCode.Header.COPY")}</h2>
    <p>{@html I18n.t("LoginCode.Info.COPY", {email: $user.email})}</p>

    <CodeValidation verify={verifyCode}
                    size={6}
                    validate={val => !isNaN(val)}
                    intermediateCallback={valueCallback}/>

    {#if wrongCode}
        <p class="error">{I18n.t("LoginCode.Error.COPY")}</p>
    {/if}

    <Button label={I18n.t("LoginCode.Continue.COPY")}
            onClick={verifyCode}
            disabled={true}/>

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
