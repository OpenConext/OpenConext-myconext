<script>

    import I18n from "i18n-js";
    import logo from "./logo.svg";

    const changeLanguage = lang => () => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        urlSearchParams.set("lang", lang);
        window.location.search = urlSearchParams.toString();
    }
</script>

<style>

    .header {
        width: 100%;
        display: flex;
        background-color: #e7e7e7;
        align-items: center;
        align-content: center;
        position: relative;
        align-items: center;
        justify-content: center;

    }

    .logo {
        padding: 10px 0;
    }

    ul {
        position: absolute;
        right: 40px;
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
<div class="header">
    <div class="logo">
        {@html logo}
    </div>

    <ul>
        <li class:active="{I18n.locale == 'en'}">
            <a href="/en" on:click|preventDefault|stopPropagation={changeLanguage("en")}>EN</a>
        </li>
        <li class:active="{I18n.locale == 'nl'}">
            <a href="/nl" on:click|preventDefault|stopPropagation={changeLanguage("nl")}>NL</a>
        </li>
    </ul>
</div>
