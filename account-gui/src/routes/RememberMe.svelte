<script>
    import I18n from "i18n-js";
    import {onMount} from "svelte";
    import {rememberMe} from "../api";
    import Button from "../components/Button.svelte";
    import {conf, links} from "../stores/conf";
    import {proceed} from "../utils/sso";
    import ButtonContainer from "../components/ButtonContainer.svelte";

    let hash = null;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        hash = urlSearchParams.get("h");
        $links.displayBackArrow = false;
    });

    const doRememberMe = () => {
        rememberMe(hash).then(() => {
            proceed($conf.magicLinkUrl)
        })
    }

</script>

<style lang="scss">
    p.explanation {
        margin: 15px 0;
    }
</style>
<h2 class="header">{I18n.t("rememberMe.header")}</h2>
<p class="explanation">{I18n.t("rememberMe.info")}</p>
<ButtonContainer>
    <Button href={"/no"} onClick={() => proceed($conf.magicLinkUrl)}
            label={I18n.t("rememberMe.no")}/>
    <Button href={"/yes"} onClick={doRememberMe}
            label={I18n.t("rememberMe.yes")}/>

</ButtonContainer>
