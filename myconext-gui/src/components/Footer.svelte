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
        margin-top: auto;
        background-color: #e7e7e7;
        padding: 18px;
    }
    .inner {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin: 0 auto;
        max-width: 1080px;
    }

    .help, .info {
        display: flex;
        flex-direction: column;
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
        font-size: 20px;
    }

    li a {
        text-decoration: none;
        color: #a3a3a3;
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
