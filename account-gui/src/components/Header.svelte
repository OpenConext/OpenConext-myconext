<script>

    import I18n from "i18n-js";
    import logo from "../img/logo_SURFconext_blue.svg";
    import openConextLogo from "../img/logo_OPEN_conext_blue.svg";

</script>

<style>

    .header {
        width: 100%;
        max-width: 835px;
        margin: 0 auto;
        display: flex;
        background-color: #006cb8;
        align-items: center;
        align-content: center;
        position: relative;
        justify-content: center;
        height: 110px;
    }

    .logo {
        padding: 10px 0;
    }

</style>
<div class="header">

    <div class="logo">
        {#if I18n.branding !== "SURFconext"}
            {@html openConextLogo}
        {:else}
            {@html logo}
        {/if}
    </div>

</div>
