<script>
    import I18n from "i18n-js";

</script>

<style>
    .not-found {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
    }

    h1 {
        color: #fe675c;
        font-size: 128px;
    }

    h2 {
        font-family: Proxima Nova, sans-serif;
        font-size: 66px;
    }

</style>
<div class="not-found">
    <h1>{I18n.ts("notFound.title")}</h1>
    <h2>{I18n.ts("notFound.title2")}</h2>
</div>