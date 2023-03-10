<script>
    import I18n from "i18n-js";
    import {links, conf} from "../stores/conf";
    import Button from "../components/Button.svelte";

    $links.displayBackArrow = false;
    export let action;

</script>

<style>
    h1 {
        margin: 16px 0;
        color: var(--color-primary-green);
        font-size: 32px;
        font-weight: bold;
    }
    p.info {
        margin-bottom: 25px;
    }
    p.hidden {
        display: none;
    }

</style>
<div class="home">
    <div class="card">
        <h1>{I18n.t("redirectMobileApp.title")}</h1>
        <p class="info">{@html I18n.t("redirectMobileApp.info")}</p>
        <p class="hidden">{action}</p>
        <Button href="/eduid" onClick={() => window.location.href = $conf.mobileAppRedirect}
                label={I18n.t("redirectMobileApp.proceedLink")}/>
    </div>
</div>