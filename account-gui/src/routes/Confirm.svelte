<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {user} from "../stores/user";
    import Button from "../components/Button.svelte";

    let email = null;
    let serviceName = null;

    onMount(() => {
        if (typeof window !== "undefined") {
            const urlSearchParams = new URLSearchParams(window.location.search);
            email = decodeURIComponent(urlSearchParams.get("email"));
            serviceName = decodeURIComponent(urlSearchParams.get("name"));
        }
    });

    const proceed = () => {
        if (typeof window !== "undefined") {
            const urlSearchParams = new URLSearchParams(window.location.search);
            const redirect = decodeURIComponent(urlSearchParams.get("redirect"));
            window.location.href = `${redirect}?h=${urlSearchParams.get('h')}`;
        }

    };
</script>

<style>
    h1 {
        font-size: 52px;
        font-weight: bold;
    }

    h2 {
        margin-bottom: 25px;
        font-size: 36px;
        font-weight: bold;
        color: var(--color-primary-green);
    }

    p {
        color: #767676;
        margin-bottom: 35px;
    }

</style>
<div class="home">
    <div class="card">
        <h1>{I18n.ts("confirm.header")}</h1>
        <h2>{I18n.ts("confirm.header2")}</h2>
        <p>{I18n.ts("confirm.thanks")}</p>
        <p>{I18n.ts("confirm.info", {name: serviceName})}</p>
        <Button href="/proceed" onClick={proceed}
                className="full"
                active={true} label={I18n.ts("confirm.link")}/>
    </div>
</div>