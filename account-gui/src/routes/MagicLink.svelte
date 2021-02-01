<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";
    import {onMount} from "svelte";
    import backArrow from "../icons/arrow-left.svg";
    import Spinner from "../components/Spinner.svelte";
    import {resendMagicLinkMail, successfullyLoggedIn} from "../api";
    import {conf} from "../stores/conf";
    import {status} from "../validation/loginStatus";

    const gmail = "/img/get-started-icon-gmail@2x-e80b706.png";
    const outlook = "/img/get-started-icon-outlook-55f9ac5.png";
    const resendMailAllowedTimeOut = 15 * 1000;

    export let id;
    let serviceName;
    let modus;
    let verificationCode = "";
    let loginStatus = status.NOT_LOGGED_IN;
    let counter = 0;
    let timeOutSeconds = 1;
    let timeOutReached = false;
    let allowedToResend = false;
    let mailHasBeenResend = false;

    onMount(() => {
        const urlParams = new URLSearchParams(window.location.search);
        serviceName = urlParams.get("name");
        modus = urlParams.get("modus");
        setTimeout(isLoggedIn, timeOutSeconds * 1000);
        setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
    });

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
        window.location.href = `${$conf.continueAfterLoginUrl}?id=${id}&code=${verificationCode}`;
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

</script>

<style>

    .back-container {
        position: absolute;
        left: 15px;
        top: 15px;
        cursor: pointer;
    }

    div.magic-link {
        margin-top: 40px;
        display: flex;
        flex-direction: column;
        align-items: center;
        align-content: center;
    }

    p {
        text-align: center;
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

</style>
{#if timeOutReached}
    <div class="magic-link">
        <h2 class="header">{I18n.t("magicLink.timeOutReached")}</h2>
        <p>{@html I18n.t("magicLink.timeOutReachedInfo")}</p>
    </div>

{:else if loginStatus === status.NOT_LOGGED_IN}
    <div class="back-container">
        <a href={`/login/${id}?name=${serviceName}&modus=${modus}`}>{@html backArrow}</a>
    </div>

    <div class="magic-link">
        <h2 class="header">{I18n.t("magicLink.header")}</h2>
        <p>{@html I18n.t("magicLink.info", {email: $user.email})}</p>
        <div class="spinner-container">
            <Spinner relative={true}/>
            <p>{I18n.t("magicLink.awaiting")}</p>
        </div>
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
        <div class="spam">
            <span>{I18n.t("magicLink.spam")}</span>
        </div>
        <div class="resend-mail">
            {#if allowedToResend}
                <span class="link" on:click={resendMail}>{I18n.t("magicLink.resend")}</span>
                <a href="resend" on:click|preventDefault|stopPropagation={resendMail}>{I18n.t("magicLink.resendLink")}</a>
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
{/if}
