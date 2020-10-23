<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";
    import {onMount} from "svelte";
    import backArrow from "../icons/arrow-left.svg";

    const gmail = "/img/get-started-icon-gmail@2x-e80b706.png";
    const outlook = "/img/get-started-icon-outlook-55f9ac5.png";

    export let id;
    let serviceName;
    let modus;

    onMount(() => {
        const urlParams = new URLSearchParams(window.location.search);
        serviceName = urlParams.get("name");
        modus = urlParams.get("modus");
    });

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
        margin-top: 75px;
        align-items: center;
        align-content: center;
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

    div.spam {
        margin-top: 30px;
        font-size: 15px;
    }

</style>
<div class="back-container">
    <a href={`/login/${id}?name=${serviceName}&modus=${modus}`}>{@html backArrow}</a>
</div>

<div class="magic-link">
    <h2 class="header">{I18n.t("magicLink.header")}</h2>
    <p>{@html I18n.t("magicLink.info", {email: $user.email})}</p>
    <div class="mail-clients">
        <div class="mail-client gmail">
            <img src={gmail} alt="gmail" width="26px" on:click={() => window.location.href="https://www.gmail.com"}/>
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

</div>

