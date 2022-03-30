<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {conf, links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import Verification from "../components/Verification.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {fetchServiceNameByHash} from "../api";
    import Cookies from "js-cookie";
    import {cookieNames} from "../constants/cookieNames";

    let serviceName = null;
    let explanation = null;
    let showSpinner = true;
    let isNew = false;

    $links.userLink = false;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);

        explanation = "validate_names";//urlSearchParams.get("explanation");
        isNew = urlSearchParams.get("new") === "true"

        const hash = urlSearchParams.get('h');
        const email = decodeURIComponent(urlSearchParams.get('email'));
        if (email) {
            Cookies.set(cookieNames.USERNAME, email, {
                expires: 365,
                secure: true,
                sameSite: "Lax"
            });
        }
        fetchServiceNameByHash(hash).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
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
{#if showSpinner}
    <Spinner/>
{/if}

<h2>{I18n.t("confirm.header")}</h2>
<p class="info">{I18n.t("confirm.thanks")}</p>
{#if explanation}
    <Verification explanation={explanation} verified={true}/>
{/if}
<Button href="/proceed" onClick={proceed}
        className="full"
        label={I18n.t("confirmStepup.proceed", {name: serviceName})}/>
