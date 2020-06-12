<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {conf} from "../stores/conf";
    import Button from "../components/Button.svelte";

    export let id;
    let serviceName = null;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        serviceName = decodeURIComponent(urlSearchParams.get("name"));
    });

    const retry = () => {
        window.location.href = `/myconext/api/idp/oidc/account/${id}?forceAuth=true`;
    };

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

    div.last {
        margin-top: 25px;
    }

</style>
<div class="home">
    <div class="card">
        <h2>{I18n.ts("affiliationMissing.header")}</h2>
        <p class="info">{I18n.ts("affiliationMissing.info")}</p>
        <p class="info">{I18n.ts("affiliationMissing.proceed", {name: serviceName})}</p>

        <Button href="/proceed" onClick={proceed}
                className="cancel"
                label={I18n.ts("affiliationMissing.proceedLink")}/>
        <div class="last">
            <Button href="/retry" onClick={retry}
                    label={I18n.ts("affiliationMissing.retryLink")}/>
        </div>

    </div>
</div>