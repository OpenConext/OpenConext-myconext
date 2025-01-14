<script>

    import I18n from "../locale/I18n";
    import Cookies from "js-cookie";
    import surfLogo from "../img/logo-surf.svg?raw";
    import {config} from "../stores/user";

    const changeLanguage = lang => () => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        urlSearchParams.set("lang", lang);
        Cookies.set("lang", lang, {expires: 365, secure: true, sameSite: "Lax", domain: $config.domain});
        window.location.search = urlSearchParams.toString();
    };

    let isEn = I18n.currentLocale() === "en";
    let helpUrl = isEn ? "/help_en/" : "/help/";
    let privacyUrl = isEn ? "https://eduid.nl/privacy-policy/" : "https://eduid.nl/privacy/";
    let termsUrl = isEn ? "https://eduid.nl/terms-of-use/" : "https://eduid.nl/gebruiksvoorwaarden/";

</script>

<style>

    .footer {
        width: 100%;
        max-width: var(--width-app);
        height: 122px;
        margin: 0 auto;
    }

    @media (max-width: 600px) {
        div.info span {
            display: none;
        }
    }

    .inner-container {
        display: flex;
        justify-content: space-between;
        margin: 0 auto;
        width: 100%;
        font-size: 16px;
        padding-top: 18px;
    }

    .help {
        display: flex;
        flex-direction: column;
        padding-left: 10px;
    }

    .terms {
        display: flex;
        flex-direction: row;
    }

    @media (max-width: 580px) {
        .terms {
            display: none;
        }
    }


    .terms span {
        color: #0077c8;
        display: inline-block;
        margin: 0 5px;
    }

    .info {
        text-align: right;
        display: flex;
        flex-direction: row;
        padding-right: 10px;
    }

    span {
        display: inline-block;
        margin-right: 10px;
    }

    ul {
        list-style: none;
    }

    li {
        display: inline-block;
        padding: 0 10px;
    }

    li:last-child {
        border-left: 1px solid black;
    }

    li.non_active a {
        color: black;
        font-weight: normal;
    }

    li.active a {
        font-weight: bold;
        color: black;
        cursor: not-allowed;
    }


</style>
<div class="footer">
    <div class="inner-container">
        <div class="help">
            <div class="terms">
                <a href={privacyUrl} target="_blank">{I18n.t("Footer.Privacy.COPY")}</a>
                <span>|</span>
                <a href={termsUrl} target="_blank">{I18n.t("Footer.Terms.COPY")}</a>
            </div>
            <a href={helpUrl} target="_blank">{I18n.t("Footer.Help.COPY")}</a>
        </div>

        <ul>
            <li class="{I18n.currentLocale() === 'en' ? 'active' : 'non_active'}">
                <a href="/en" on:click|preventDefault|stopPropagation={changeLanguage("en")}>EN</a>
            </li>
            <li class="{I18n.currentLocale() === 'nl' ? 'active' : 'non_active'}">
                <a href="/nl" on:click|preventDefault|stopPropagation={changeLanguage("nl")}>NL</a>
            </li>
        </ul>

        <div class="info">
            <span>{I18n.t("Footer.PoweredBy.COPY")}</span>
            <a href="https://www.surf.nl/" target="_blank">{@html surfLogo}</a>
        </div>
    </div>
</div>
