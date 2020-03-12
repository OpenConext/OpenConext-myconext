<script>
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";
    import {conf} from "../stores/conf";

    const migrate = () => {
        if (typeof window !== "undefined") {
            window.location.href = `${$conf.migrationUrl}&lang=${I18n.locale}`;
        }
    };

</script>

<style>


    h1 {
        margin: 6px 0 35px 0;
        text-align: center;
    }

    p {
        margin-bottom: 35px;
    }

</style>
<div class="migration">
    <div class="card">
        <h1>{@html I18n.ts("migration.header")}</h1>
        <p>{I18n.ts("migration.info1")}</p>
        <p>{I18n.ts("migration.info2")}</p>
        <Button href="/migrate" onClick={migrate} className="full" label={I18n.ts("migration.link")}/>
    </div>
</div>