<script>
    import I18n from "i18n-js";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import ImageContainer from "../components/ImageContainer.svelte";
    import icon from "../icons/redesign/undraw_Order_confirmed_re_g0if.svg";
    import {fetchServiceNameByHash} from "../api";
    import Button from "../components/Button.svelte";
    import {conf} from "../stores/conf";
    import {proceed} from "../utils/sso";

    let serviceName = null;
    let showSpinner = true;
    let hash = null;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        hash = urlSearchParams.get("h")
        fetchServiceNameByHash(hash).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

</script>

<style lang="scss">

</style>
{#if showSpinner}
    <Spinner/>
{/if}

<h2 class="header">{I18n.t("congrats.header")}</h2>
<p class="explanation">{I18n.t("congrats.info")}</p>
<ImageContainer icon={icon} margin={true}/>
<Button href={"/next"} onClick={() => proceed($conf.magicLinkUrl)}
        label={I18n.t("congrats.next", {name: serviceName})}/>
