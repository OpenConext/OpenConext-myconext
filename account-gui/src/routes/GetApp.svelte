<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import Spinner from "../components/Spinner.svelte";
    import appStore from "../icons/redesign/Download_on_the_App_Store_Badge.svg";
    import googlePlay from "../icons/redesign/Google_Play-Badge-Logo.wine.svg";
    import ButtonContainer from "../components/ButtonContainer.svelte";
    import {navigate} from "svelte-routing";

    let showSpinner = true;
    let hash = null;

    onMount(() => {
        $links.displayBackArrow = false;

        const urlSearchParams = new URLSearchParams(window.location.search);
        hash = urlSearchParams.get("h");
        showSpinner = false;
    });

    const next = () => {
        navigate(`/enrollapp?h=${hash}`)
    };

    const back = () => {
        window.history.back();
    };

</script>

<style>

    p.explanation {
        margin: 15px 0;
        font-size: 14px;
    }
    .store-icons {
        display: flex;
    }
    :global(.store-icons a.apple svg) {
        width: 158px;
        height: auto;
        margin-top: 11px;
        margin-left: 3px;
    }
    :global(.store-icons a.google svg) {
        width: auto;
        height: 68px;
        margin-left: -23px;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}

<h2 class="header">{I18n.t("getApp.header")}</h2>
<p class="explanation">{@html I18n.t("getApp.info")}</p>
<div class="store-icons">
    <a class="google" href={I18n.t("getApp.google")} target="_blank">
        {@html googlePlay}
    </a>
    <a class="apple" href={I18n.t("getApp.apple")} target="_blank">
        {@html appStore}
    </a>
</div>
<p class="explanation">{@html I18n.t("getApp.after")}</p>
<ButtonContainer>
    <Button className="cancel" href={I18n.t("getApp.back")} onClick={back}
            label={I18n.t("getApp.back")}/>
    <Button href={I18n.t("getApp.next")} onClick={next}
            label={I18n.t("getApp.next")}/>
</ButtonContainer>

