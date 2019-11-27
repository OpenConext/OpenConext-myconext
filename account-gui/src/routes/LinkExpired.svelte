<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";

</script>

<style>
    h2 {
        margin-bottom: 40px;
        font-size: 38px;
    }
    p {
        color: var(--color-primary-black);
    }

</style>
<div class="home">
    <div class="card">
        <h2>{I18n.ts("expired.title")}</h2>
        <p>{@html I18n.ts("expired.info")}</p>
    </div>
</div>