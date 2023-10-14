<script>
    import I18n from "i18n-js";

    export let institution = {};
</script>
<style lang="scss">
div.validated-data {
    margin: 20px 0 10px 20px;
}
</style>
<div class="validated-data">
    <table>
        <thead/>
        <tbody>
        <tr>
            <td class="attribute">{I18n.t("profile.givenName")}:</td>
            <td>{institution.givenName}</td>
        </tr>
        <tr>
            <td>{I18n.t("profile.familyName")}:</td>
            <td>{institution.familyName}</td>
        </tr>
        </tbody>
    </table>
</div>