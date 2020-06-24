<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {conf} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import Verification from "../components/Verification.svelte";

    let email = null;
    let serviceName = null;
    let explanation = null;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        email = decodeURIComponent(urlSearchParams.get("email"));
        serviceName = decodeURIComponent(urlSearchParams.get("name"));
        explanation = urlSearchParams.get("explanation");
    });

    const proceed = () => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const redirect = decodeURIComponent(urlSearchParams.get("redirect"));
        //Ensure we are not attacked by an open redirect
        if (redirect.startsWith($conf.magicLinkUrl)) {
            window.location.href = `${redirect}?h=${urlSearchParams.get('h')}`;
        } else {
            throw new Error("Invalid redirect: " + redirect);
        }
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
        <h2>{I18n.t("confirm.header")}</h2>
        <p class="info">{I18n.t("confirm.thanks")}</p>
        {#if explanation}
            <Verification explanation={explanation} verified={true}/>
        {/if}
        <Button href="/proceed" onClick={proceed}
                className="full"
                label={I18n.t("confirmStepup.proceed", {name: serviceName})}/>
    </div>
</div>