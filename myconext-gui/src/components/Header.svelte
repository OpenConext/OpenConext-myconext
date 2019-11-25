<script>
    import I18n from "i18n-js";
    import logo from "./logo.svg";

</script>

<style>

    .header {
        width: 100%;
        display: flex;
        background-color: #e7e7e7;
        align-items: center;
        align-content: center;
        position: relative;
        padding-left: 40px;

    }

    .logo {
        padding: 10px 0;
    }

    h2  {
        margin-left: 40px;
    }

</style>
<div class="header">
    <div class="logo">
        <a href="/">{@html logo}</a>

    </div>
    <h2>{I18n.t("header.title")}</h2>

</div>
