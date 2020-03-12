<script>

    import I18n from "i18n-js";
    import Cookies from "js-cookie";
    import surfLogo from "../img/logo-surf.svg";
    import {config} from "../stores/user";

    const changeLanguage = lang => () => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        urlSearchParams.set("lang", lang);
        Cookies.set("lang", lang, {expires: 365, secure: true, sameSite: "Lax", domain: $config.domain});
        window.location.search = urlSearchParams.toString();
    };

    let isEn = I18n.locale === "en";
    let helpUrl = isEn ? "/help_en/" : "/help/";
    let privacyUrl = isEn ? "/privacy_policy/" : "/privacyverklaring/";
    let termsUrl = isEn ? "/terms_of_service/" : "/voorwaarden/";

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

    .inner {
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
    <div class="inner">
        <div class="help">
            <div class="terms">
                <a href={privacyUrl} target="_blank">{I18n.t("footer.privacy")}</a>
                <span>|</span>
                <a href={termsUrl} target="_blank">{I18n.t("footer.terms")}</a>
            </div>
            <a href={helpUrl} target="_blank">{I18n.t("footer.help")}</a>
        </div>

        <ul>
            <li class="{I18n.locale === 'en' ? 'active' : 'non_active'}">
                <a href="/en" on:click|preventDefault|stopPropagation={changeLanguage("en")}>EN</a>
            </li>
            <li class="{I18n.locale === 'nl' ? 'active' : 'non_active'}">
                <a href="/nl" on:click|preventDefault|stopPropagation={changeLanguage("nl")}>NL</a>
            </li>
        </ul>

        <div class="info">
            <span>{I18n.t("footer.poweredBy")}</span>
            <a href="https://www.surf.nl/" target="_blank">{@html surfLogo}</a>
        </div>
    </div>
</div>
