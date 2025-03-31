<script>

    import I18n from "../locale/I18n";
    import Cookies from "js-cookie";
    import surfLogo from "../img/logo-surf.svg?raw";
    import {conf} from "../stores/conf";

    const changeLanguage = lang => () => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        urlSearchParams.set("lang", lang);
        const domain = $conf.domain;
        Cookies.set("lang", lang, {
            expires: 365,
            secure: window.location.protocol.startsWith("https"),
            sameSite: "Lax",
            domain: domain
        });
        window.location.search = urlSearchParams.toString();
    };

    let isEn = I18n.currentLocale() === "en";
    let privacyUrl = isEn ? "https://eduid.nl/privacy-policy/" : "https://eduid.nl/privacy/";
    let termsUrl = isEn ? "https://eduid.nl/terms-of-use/" : "https://eduid.nl/gebruiksvoorwaarden/";

</script>

<style>

    .footer {
        width: 100%;
        max-width: var(--width-app);
        height: 122px;
        margin: 0 auto;
        color: white;
    }

    @media (max-width: 600px) {
        div.info span {
            display: none;
        }
    }

    @media (max-width: 800px) {
        .footer {
            max-width: none;
        }
    }

    .inner {
        display: flex;
        justify-content: space-between;
        margin: 0 auto;
        width: 100%;
        font-size: 14px;
        padding-top: 18px;
    }

    .help {
        display: flex;
        flex-direction: column;
        padding-left: 10px;
    }

    .help a.white {
        color: white;
        line-height: 22px;
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
        border-left: 1px solid white;
    }

    li.non_active a {
        color: white;
        font-weight: normal;
    }

    li.active a {
        font-weight: 600;
        color: white;
        cursor: not-allowed;
    }


</style>
<div class="footer">
    <div class="inner">
        <div class="help">
            <a class="white" href={termsUrl} target="_blank">{I18n.t("Footer.Terms.COPY")}</a>
            <a class="white" href={privacyUrl} target="_blank">{I18n.t("Footer.Privacy.COPY")}</a>
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
            <a href="https://surf.nl" target="_blank">{@html surfLogo}</a>
        </div>
    </div>
</div>
