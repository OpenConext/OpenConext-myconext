<script>
    import I18n from "../locale/I18n";
    import {links} from "../stores/conf";

    $links.displayBackArrow = false;

</script>

<style>
    h1 {
        margin: 16px 0;
        color: var(--color-primary-green);
        font-size: 32px;
        font-weight: 600;

    }

</style>
<div class="home">
    <div class="card">
        <h1>{I18n.t("success.title")}</h1>
        <p>{@html I18n.t("success.info")}</p>
    </div>
</div>