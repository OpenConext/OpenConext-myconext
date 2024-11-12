<script>
    import I18n from "../locale/I18n";
    import {user} from "../stores/user";
    import {onMount} from "svelte";
    import backArrow from "../icons/arrow-left.svg";
    import Spinner from "../components/Spinner.svelte";
    import {resendMagicLinkMail, successfullyLoggedIn} from "../api";
    import {conf, links} from "../stores/conf";
    import {status} from "../constants/loginStatus";
    import Button from "../components/Button.svelte";
    import {validVerificationCode} from "../constants/regexp";
    import critical from "../icons/critical.svg";
    import DOMPurify from "dompurify";

    const gmail = "/img/get-started-icon-gmail@2x-e80b706.png";
    const outlook = "/img/get-started-icon-outlook-55f9ac5.png";
    const resendMailAllowedTimeOut = $conf.emailSpamThresholdSeconds * 1000;

    export let id;
    let serviceName;
    let modus;
    let verificationCode = "";
    let loginStatus = status.NOT_LOGGED_IN
    let counter = 0;
    let timeOutSeconds = 1;
    let timeOutReached = false;
    let allowedToResend = false;
    let mailHasBeenResend = false;
    let verificationCodeError = false;
    let onMobile = "ontouchstart" in document.documentElement;

    onMount(() => {
        $links.displayBackArrow = false;

        const urlParams = new URLSearchParams(window.location.search);
        verificationCodeError = urlParams.get("mismatch") === "true";
        if (verificationCodeError) {
           loginStatus = status.LOGGED_IN_DIFFERENT_DEVICE;
        }
        serviceName = urlParams.get("name");
        modus = urlParams.get("modus");
        setTimeout(isLoggedIn, timeOutSeconds * 1000);
        setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
    });

    const init = el => el.focus();

    const resendMail = () => {
        allowedToResend = false;
        resendMagicLinkMail(id).then(() => {
            mailHasBeenResend = true;
            //reset timeout
            counter = 1;
            timeOutSeconds = 1;
            setTimeout(() => {
                allowedToResend = true;
                mailHasBeenResend = false;
            }, resendMailAllowedTimeOut);

        });
    }

    const verify = () => {
        const location = window.location.href.replace(/&mismatch=true/g, "");
        window.location.href = `${$conf.continueAfterLoginUrl}?id=${id}&verificationCode=${verificationCode}&currentUrl=${encodeURIComponent(location)}`;
    }

    const isLoggedIn = () => {
        successfullyLoggedIn(id).then(res => {
            loginStatus = res;
            if (loginStatus === status.LOGGED_IN_DIFFERENT_DEVICE) {
                //First check the verification code that was send
            } else if (loginStatus === status.NOT_LOGGED_IN) {
                ++counter;
                if (counter % 30 === 0) {
                    //timeOuts 1, 3, 6, 10 with every timeOut applied 30 times results in total of 10 minutes
                    const base = (counter / 30) + 1;
                    timeOutSeconds = ((0.5 * Math.pow(base, 2)) + (0.5 * base));
                }
                if (counter > (30 * 4)) {
                    timeOutReached = true;
                } else {
                    setTimeout(isLoggedIn, timeOutSeconds * 1000);
                }
            }
        });
    }

    const updateVerificationCode = e => {
        let value = e.target.value.toUpperCase().trim();
        if ((value.length > verificationCode.length && value.length === 3) || (value.length >= 4 && value.indexOf("-") === -1)) {
            value = value.substring(0, 3) + "-" + value.substring(3);
        }
        if ((value.match(/-/g) || []).length > 1) {
            value = value.replace("-", "");
        }
        if (value.length > 7) {
            value = value.substring(0, 7);
        }
        verificationCode = value;
        e.target.value = verificationCode;
    }

    const handleVerificationCodeEnter = e => {
        if (e.key === "Enter" && validVerificationCode(e.target.value)) {
            verify();
        }
    }

</script>

<style lang="scss">

    div.magic-link {
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

    input[type=text] {
        border: 1px solid #727272;
        border-radius: 6px;
        padding: 14px;
        font-size: 16px;
        width: 100%;
        margin: 8px 0 35px 0;
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
        font-weight: bold;
    }

</style>
{#if timeOutReached}
    <div class="magic-link">
        <h2 class="header">{I18n.t("magicLink.timeOutReached")}</h2>
        <p>{@html I18n.t("magicLink.timeOutReachedInfo")}</p>
    </div>

{:else if loginStatus === status.NOT_LOGGED_IN}
    <div class="magic-link">
        <h2 class="header">{I18n.t("magicLink.header")}</h2>
            <p>{I18n.t("magicLink.info")}<span class="email">{$user.email}.</span></p>
        <div class="spinner-container">
            <Spinner relative={true}/>
            <p>{I18n.t("magicLink.awaiting")}</p>
        </div>
        {#if !onMobile}
            <div class="mail-clients">
                <div class="mail-client gmail">
                    <img src={gmail} alt="gmail" width="26px"
                         on:click={() => window.location.href="https://www.gmail.com"}/>
                    <a href="https://www.gmail.com">{I18n.t("magicLink.openGMail")}</a>
                </div>
                <div class="mail-client outlook">
                    <img src={outlook} alt="outlook" on:click={() => window.location.href="https://outlook.live.com/owa/"}/>
                    <a href="https://outlook.live.com/owa/">{I18n.t("magicLink.openOutlook")}</a>
                </div>
            </div>
        {/if}
        <div class="spam">
            <span>{I18n.t("magicLink.spam")}</span>
        </div>
        <div class="resend-mail">
            {#if allowedToResend}
                <span class="link" on:click={resendMail}>{I18n.t("magicLink.resend")}</span>
                <a href="resend"
                   on:click|preventDefault|stopPropagation={resendMail}>{I18n.t("magicLink.resendLink")}</a>
            {:else if mailHasBeenResend}
                <span>{I18n.t("magicLink.mailResend")}</span>
            {/if}

        </div>

    </div>
{:else if loginStatus === status.LOGGED_IN_SAME_DEVICE}
    <div class="magic-link">
        <h2 class="header">{I18n.t("magicLink.loggedIn")}</h2>
        <p>{@html I18n.t("magicLink.loggedInInfo")}</p>
    </div>
{:else if loginStatus === status.LOGGED_IN_DIFFERENT_DEVICE}
    <div class="magic-link no-center ">
        <h2 class="header">{I18n.t("magicLink.loggedInDifferentDevice")}</h2>
        <p class="no-center">{@html I18n.t("magicLink.loggedInDifferentDeviceInInfo")}</p>
        <p class="no-center">{@html I18n.t("magicLink.loggedInDifferentDeviceInInfo2")}</p>
        <input class="verification-code"
               type="text"
               spellcheck="false"
               use:init
               value={verificationCode}
               on:input={updateVerificationCode}
               on:keydown={handleVerificationCodeEnter}>
        {#if verificationCodeError}
            <div class="error">
                <span class="svg">{@html critical}</span>
                <div>
                    <span>{I18n.t("magicLink.verificationCodeError")}</span>
                </div>
            </div>
        {/if}
        <Button label={I18n.t("magicLink.verify")} onClick={verify}
                disabled={!validVerificationCode(verificationCode)}/>
    </div>
{/if}
