<script>
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";
    import {conf} from "../stores/conf";

    const migrate = () => {
        if (typeof window !== "undefined") {
            window.location.href = `${$conf.migrationUrl}`;
        }
    };

</script>

<style>


    h2 {
        margin: 6px 0 35px 0;
        color: var(--color-primary-green);
    }

    p {
        margin-bottom: 35px;
    }

</style>
<div class="migration">
    <div class="card">
        <h1>{I18n.ts("migration.header")}</h1>
        <h2>{I18n.ts("migration.header2")}</h2>
        <p>{@html I18n.ts("migration.info")}</p>
        <Button href="/migrate" onClick={migrate} className="full" label={I18n.ts("migration.link")}/>
    </div>
</div>