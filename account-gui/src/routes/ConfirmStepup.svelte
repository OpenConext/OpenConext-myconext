<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {conf} from "../stores/conf";
    import Button from "../components/Button.svelte";

    let email = null;
    let serviceName = null;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        serviceName = decodeURIComponent(urlSearchParams.get("name"));
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
        <h2>{I18n.t("confirmStepup.header")}</h2>
        <p class="info">{I18n.t("confirmStepup.thanks")}</p>
        <Button href="/proceed" onClick={proceed}
                className="full"
                label={serviceName}/>
    </div>
</div>