<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {conf} from "../stores/conf";
    import Button from "../components/Button.svelte";

    export let id;
    let existing = null;
    let serviceName = null;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        existing = "true" === urlSearchParams.get("existing");
        serviceName = decodeURIComponent(urlSearchParams.get("name"));
    });

    const proceed = () => {
        window.location.href = `/myconext/api/idp/oidc/account/${id}`;
    };
</script>

<style>


    h2 {
        margin: 30px 0 40px 0;
        font-size: 32px;
        color: var(--color-primary-green);
    }

    p.info {
        margin-bottom: 25px;
    }

</style>
<div class="home">
    <div class="card">
        <h2>{I18n.t("stepup.header")}</h2>
        <p class="info">{I18n.t("stepup.info", {name: serviceName})}</p>
        <p class="info">{I18n.t("stepup.proceed")}</p>
        <Button href="/proceed" onClick={proceed}
                className="full"
                label={I18n.t("stepup.link")}/>
    </div>
</div>