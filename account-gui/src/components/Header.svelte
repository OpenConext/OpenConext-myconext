<script>

    import I18n from "i18n-js";
    import logo from "./logo.svg";
    import openConextLogo from "./open_conext.svg";

</script>

<style>

    .header {
        width: 100%;
        max-width: 500px;
        margin: 0 auto;
        display: flex;
        background-color: #e7e7e7;
        align-items: center;
        align-content: center;
        position: relative;
        justify-content: center;
    }

    .logo {
        padding: 10px 0;
    }

</style>
<div class="header">
    {#if I18n.branding !== "SURFconext"}
        <a href="/">{@html openConextLogo}</a>
    {:else}
        <a href="/">{@html logo}</a>
    {/if}

    <div class="logo">
        {@html logo}
    </div>

</div>
