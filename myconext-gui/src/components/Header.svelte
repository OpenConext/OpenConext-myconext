<script>
    import I18n from "i18n-js";
    import logo from "./logo.svg";
    import openConextLogo from "./open_conext.svg";
    import {config} from "../stores/user";
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

        color: #000000;
    }

    .logo {
        padding: 10px 0;
    }

    h2 {
        margin-left: 40px;
    }

</style>
<div class="header">
    <div class="logo">
        {#if $config.branding !== "SURFconext"}
            <a href="/">{@html openConextLogo}</a>
        {:else}
            <a href="/">{@html logo}</a>
        {/if}

    </div>
    <h2>{I18n.ts("header.title")}</h2>

</div>
