<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {conf} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import oneMoreThing from "../icons/onemorething_filled.svg";
    import Verification from "../components/Verification.svelte";

    let email = null;
    let serviceName = null;
    let explanation = null;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        serviceName = decodeURIComponent(urlSearchParams.get("name"));
        explanation = decodeURIComponent(urlSearchParams.get("explanation"));
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
        margin: 30px 0;
        font-size: 32px;
        color: var(--color-primary-green);
    }

</style>
<div class="home">
    <div class="card">
        <h2>{I18n.t("confirmStepup.header")}</h2>
        <Verification explanation={explanation} verified={true}/>
        <Button href="/proceed" onClick={proceed}
                className="full"
                label={I18n.t("confirmStepup.proceed", {name: serviceName})}/>

    </div>
</div>