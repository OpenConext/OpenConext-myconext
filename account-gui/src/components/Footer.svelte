<script>

    import I18n from "i18n-js";
    import Cookies from "js-cookie";
    import surfLogo from "../img/logo-surf.svg";

    const changeLanguage = lang => () => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        urlSearchParams.set("lang", lang);
        Cookies.set("lang", lang, {expires: 365, secure: true, sameSite: "strict"});
        window.location.search = urlSearchParams.toString();
    };

    let isEn = I18n.locale === "en";
    let privacyUrl = isEn ? "/privacy_policy/" : "/privacyverklaring/";
    let termsUrl = isEn ? "/terms_of_service/" : "/voorwaarden/";

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
        font-weight: bold;
        color: white;
        cursor: not-allowed;
    }


</style>
<div class="footer">
    <div class="inner">
        <div class="help">
            <a class="white" href={privacyUrl} target="_blank">{I18n.t("footer.privacy")}</a>
            <a class="white" href={termsUrl} target="_blank">{I18n.t("footer.terms")}</a>
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
            <a href="https://surfconext.nl" target="_blank">{@html surfLogo}</a>
        </div>
    </div>
</div>
