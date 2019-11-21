<script>
    import logo from "./logo_white.svg";
    import I18n from "i18n-js";
</script>

<style>
    .navbar-wrapper {
        position: relative;
        border-bottom: 2px solid #ababab;
        user-select: none;

    }
    .navbar-wrapper::after {
        content: "";
        width: 31%;
        height: 4px;
        padding: 0;
        margin: 0;
        position: absolute;
        right: 0;
        bottom: -2.5px;
        z-index: 10;
        border: none;
        background: #fff;
    }
    nav.top {
        flex-flow: row nowrap;
        justify-content: flex-start;
        position: relative;
        display: flex;
        align-items: center;
        padding: 0;
    }
    .container {
        display: flex;
        height: 68px;
        border-bottom-right-radius: 67px;
        border: unset;
        z-index: 500;
        position: relative;
        align-items: flex-start;
        justify-content: space-between;
        flex-wrap: wrap;
        max-width: 1140px;
        width: 100%;
        padding-right: 15px;
        padding-left: 15px;
        margin-right: auto;
        margin-left: auto;

    }
    a.home {
        display: flex;
        width: 200px;
        height: 88px;
        font-size: 1.25rem;
        padding: 0;
        margin: 0;
        color: #fff;
        background: #0077C8;
        border-bottom-right-radius: 12px;
        border-bottom-left-radius: 12px;
        z-index: 2;
        line-height: inherit;
        white-space: nowrap;
        text-decoration: none;
    }

    :global(.home) svg {
        width: 100%;
        height: 95%;
        padding: 15px;
    }

    h2.title {
        font-family: "Montserrat", sans-serif;
        font-weight: 400;
        line-height: 1.06;
        color: #772583;
        font-size: 24px;
        padding: 20px;
        margin: 0;
    }
    @media only screen and (max-width: 620px) {
        h2.title {
            display: none;
        }
    }

    div.nav-menu-wrapper {
        height: 100%;
        bottom: -2px;
        position: relative;
        right: 35px;
        border-bottom: 2px solid #ababab;
        border-right: 2px solid #ababab;
        border-bottom-right-radius: 67px;
        display: flex;
        flex-grow: 1
    }

</style>

<div class="navbar-wrapper">
    <nav class="top">
        <div class="container">
            <a href="/" class="home">
                {@html logo}
            </a>
            <h2 class="title">{I18n.t("header.title")}</h2>
            <div class="nav-menu-wrapper"></div>
        </div>
    </nav>
</div>