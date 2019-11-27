<script>

    import I18n from "i18n-js";
    import Cookies from "js-cookie";

    const changeLanguage = lang => () => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        urlSearchParams.set("lang", lang);
        Cookies.set("lang", lang, { expires: 365 });
        window.location.search = urlSearchParams.toString();
    }

</script>

<style>

    .footer {
        background-color: #ffffff;
        padding: 0 70px;
        width: 100%;
        max-width: var(--width-app);
        height: 122px;
        margin: 0 auto;
        border-left: 2px solid var(--color-primary-blue);
        border-bottom-left-radius: 10px;
        border-bottom-right-radius: 10px;
        border-right: 2px solid var(--color-primary-blue);
        border-bottom: 4px solid var(--color-primary-blue);
    }

    @media (max-width: 600px) {
        .footer {
            padding: 0 28px;
        }
    }

    .inner {
        display: flex;
        justify-content: space-between;
        margin: 0 auto;
        width: 100%;
        font-size: 16px;
        font-weight: 300;
        padding: 42px 0;
        border-top: 2px solid #e6e9e8;
    }

    .help, .info {
        display: flex;
        flex-direction: column;
    }
    .info {
        text-align: right;
    }
    span {
        display: inline-block;
        margin-bottom: 5px;
    }

    ul {
        list-style: none;
    }

    li {
        display: inline-block;
        padding: 0 10px;
    }
    a {
        font-weight: 400;
    }
    li:last-child {
        border-left: 1px solid black;
    }

    li.active a {
        color: black;
        font-weight: bold;
        cursor: not-allowed;

    }


</style>
<div class="footer">
    <div class="inner">
        <div class="help">
            <span>{I18n.ts("footer.tip")}</span>
            <a href="https://surfconext.nl" target="_blank">{I18n.ts("footer.help")}</a>
        </div>

        <ul>
            <li class:active="{I18n.locale == 'en'}">
                <a href="/en" on:click|preventDefault|stopPropagation={changeLanguage("en")}>EN</a>
            </li>
            <li class:active="{I18n.locale == 'nl'}">
                <a href="/nl" on:click|preventDefault|stopPropagation={changeLanguage("nl")}>NL</a>
            </li>
        </ul>

        <div class="info">
            <span>{I18n.ts("footer.poweredBy")}</span>
            <a href="https://surfconext.nl" target="_blank">{I18n.ts("footer.surfconext")}</a>
        </div>
    </div>
</div>
