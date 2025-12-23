<script>
    import {conf, links} from "../stores/conf";
    import I18n from "../locale/I18n";
    import ImageContainer from "../components/ImageContainer.svelte";
    import icon from "../icons/redesign/undraw_Order_confirmed_re_g0if 1.svg?raw";
    import Button from "../components/Button.svelte";
    import {proceed} from "../utils/sso.js";

    $links.displayBackArrow = false;

    const next = () => {
        proceed($conf.magicLinkUrl);
    }

</script>

<style>
    h1 {
        margin: 16px 0;
        color: var(--color-primary-green);
        font-size: 32px;
        font-weight: 600;

    }

</style>


<h2 class="header">{I18n.t("Login.RequestSuccessHeader.COPY")}</h2>
<ImageContainer icon={icon} margin={true} class="image-container-success" height="220px"/>
<p class="info">{I18n.t("Login.RequestSuccessInfo.COPY")}</p>
<Button href={"/"} onClick={next}
        label={I18n.t("Login.RequestSuccessNext.COPY")}/>
