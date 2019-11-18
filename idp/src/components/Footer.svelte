<script>

    import I18n from "i18n-js";

</script>

<style>

    .footer {
        margin-top: auto;
        width: 100%;
        display: flex;
        background-color: #e7e7e7;
        padding: 18px;
    }
    .help, .info {
        display: flex;
        flex-direction: column;
        margin-left: 50px;
    }
    .info {
        margin-left: auto;
        margin-right: 50px;
    }
    span {
        display: inline-block;
        margin-bottom: 5px;
    }

</style>
<div class="footer">
    <div class="help">
        <span>{I18n.t("footer.tip")}</span>
        <a href="https://surfconext.nl" target="_blank">{I18n.t("footer.help")}</a>
    </div>
    <div class="info">
        <span>{I18n.t("footer.poweredBy")}</span>
        <a href="https://surfconext.nl" target="_blank">{I18n.t("footer.surfconext")}</a>
    </div>

</div>
