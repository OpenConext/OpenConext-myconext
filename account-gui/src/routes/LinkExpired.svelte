<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";
    import Button from "../components/Button.svelte";

</script>

<style>
    h1 {
        margin: 40px 0;
        color: var(--color-primary-red);
        font-size: 32px;
        font-weight: bold;

    }
    p {
        color: var(--color-primary-black);
        margin-bottom: 40px;
    }

</style>
<div class="home">
    <div class="card">
        <h1>{I18n.t("expired.title")}</h1>
        <p>{@html I18n.t("expired.info")}</p>
        <Button label={I18n.t("expired.back")} href="https://www.eduid.nl" onClick={() => window.location.href = "https://www.eduid.nl"} />
    </div>
</div>