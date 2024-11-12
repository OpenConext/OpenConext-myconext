<script>
    import I18n from "../locale/I18n";
    import {config, user} from "../stores/user";
    import {onMount} from "svelte";
    import Spinner from "../components/Spinner.svelte";
    import {createFromInstitutionPoll, resendCreateFromInstitutionMail} from "../api";
    import {status} from "../constants/loginStatus";
    import DOMPurify from "dompurify";

    const gmail = "/img/get-started-icon-gmail@2x-e80b706.png";
    const outlook = "/img/get-started-icon-outlook-55f9ac5.png";
    const resendMailAllowedTimeOut = $config.emailSpamThresholdSeconds * 1000;

    export let hash;

    let loginStatus = status.NOT_LOGGED_IN
    let counter = 0;
    let timeOutSeconds = 1;
    let timeOutReached = false;
    let allowedToResend = false;
    let mailHasBeenResend = false;
    let onMobile = "ontouchstart" in document.documentElement;

    onMount(() => {
        setTimeout(() => isLoggedIn(), timeOutSeconds * 1000);
        setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
    });

    const resendMail = () => {
        allowedToResend = false;
        resendCreateFromInstitutionMail(hash).then(() => {
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

    const isLoggedIn = () => {
        createFromInstitutionPoll(hash).then(res => {
            loginStatus = res;
            if (loginStatus === status.NOT_LOGGED_IN) {
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

<style lang="scss">
    .poll-from-institution {
        display: flex;
        flex-direction: column;
        background-color: white;
        height: auto;
        min-height: 500px;
    }

    div.inner {
        margin: 25px auto auto 200px;
        max-width: 600px;

        @media (max-width: 800px) {
            margin: 25px auto;
        }
    }

    h3 {
        color: var(--color-primary-green);
        margin-bottom: 40px;
    }

    div.mail-clients {
        width: 100%;
        display: flex;
        padding: 25px;
        margin: 20px 0;
        align-items: center;
        align-content: center;
    }

    div.mail-client {
        display: flex;
        align-items: center;
        align-content: center;
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
        margin-left: 60px;
    }

    div.mail-clients a {
        text-decoration: none;
        color: #606060;
    }

    div.mail-clients a:hover {
        text-decoration: underline;
        color: var(--color-primary-blue);
    }

    div.resend-mail {
        margin-top: 40px;
    }


</style>
<div class="poll-from-institution">
    <div class="inner">
        {#if timeOutReached}
            <h2 class="header">{I18n.t("pollFromInstitution.timeOutReached")}</h2>
            <p>{@html I18n.t("pollFromInstitution.timeOutReachedInfo")}</p>
        {:else if loginStatus === status.NOT_LOGGED_IN}
            <Spinner relative={true} account={true}/>
            <h3 class="header">{I18n.t("pollFromInstitution.header")}</h3>
            <p>
                <span>{@html I18n.t("pollFromInstitution.info", {email: DOMPurify.sanitize($user.email)})}</span>
                <span>{I18n.t("pollFromInstitution.awaiting")}</span>
            </p>
            {#if !onMobile}
                <div class="mail-clients">
                    <div class="mail-client gmail">
                        <img src={gmail} alt="gmail" width="26px"
                             on:click={() => window.location.href="https://www.gmail.com"}/>
                        <a href="https://www.gmail.com">{I18n.t("pollFromInstitution.openGMail")}</a>
                    </div>
                    <div class="mail-client outlook">
                        <img src={outlook} alt="outlook"
                             on:click={() => window.location.href="https://outlook.live.com/owa/"}/>
                        <a href="https://outlook.live.com/owa/">{I18n.t("pollFromInstitution.openOutlook")}</a>
                    </div>
                </div>
            {/if}
            <div>
                <span>{I18n.t("pollFromInstitution.spam")}</span>
            </div>
            <div class="resend-mail">
                {#if allowedToResend}
                    <span class="link" on:click={resendMail}>{I18n.t("pollFromInstitution.resend")}</span>
                    <a href="resend"
                       on:click|preventDefault|stopPropagation={resendMail}>{I18n.t("pollFromInstitution.resendLink")}</a>
                {:else if mailHasBeenResend}
                    <span>{I18n.t("pollFromInstitution.mailResend")}</span>
                {/if}

            </div>
        {:else if loginStatus === status.LOGGED_IN_SAME_DEVICE}
            <h3 class="header">{I18n.t("pollFromInstitution.loggedIn")}</h3>
            <p>{@html I18n.t("pollFromInstitution.loggedInInfo")}</p>
        {/if}
    </div>
</div>
