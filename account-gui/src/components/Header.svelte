<script>

    import I18n from "i18n-js";
    import logo from "./logo.svg";
    import openConextLogo from "./open_conext.svg";

</script>

<style>

    .header {
        width: 100%;
        max-width: 835px;
        margin: 0 auto;
        display: flex;
        background-color: #0061b0;
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
