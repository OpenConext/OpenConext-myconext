<script>
    import I18n from "../locale/I18n";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import ImageContainer from "../components/ImageContainer.svelte";
    import icon from "../icons/redesign/undraw_Order_confirmed_re_g0if.svg?raw";
    import {fetchServiceNameByHash} from "../api";
    import Button from "../components/Button.svelte";
    import {conf, links} from "../stores/conf";
    import {proceed} from "../utils/sso";
    import Cookies from "js-cookie";
    import {cookieNames} from "../constants/cookieNames";
    import {loginPreferences} from "../constants/loginPreferences";

    let serviceName = null;
    let showSpinner = true;
    let hash = null;

    onMount(() => {
        $links.displayBackArrow = false;

        const urlSearchParams = new URLSearchParams(window.location.search);
        hash = urlSearchParams.get("h")
        fetchServiceNameByHash(hash).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const nextStep = () => {
        Cookies.set(cookieNames.LOGIN_PREFERENCE, loginPreferences.APP, {
            expires: 365,
            secure: true,
            sameSite: "Lax"
        });
        proceed($conf.magicLinkUrl);
    }

</script>

<style lang="scss">

</style>
{#if showSpinner}
    <Spinner/>
{/if}

<h2 class="header">{I18n.t("Congrats.Header.COPY")}</h2>
<p class="explanation">{I18n.t("Congrats.Info.COPY")}</p>
<ImageContainer icon={icon} margin={true}/>
<Button href={"/next"} onClick={nextStep}
        label={I18n.t("Congrats.Next.COPY", {name: serviceName})}/>
