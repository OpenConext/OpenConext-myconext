<script>
    import I18n from "../locale/I18n";
    import {onMount} from 'svelte';
    import {conf, links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {fetchServiceName} from "../api";
    import {proceed} from "../utils/sso";

    export let id;
    let serviceName = null;
    let showSpinner = true;
    let level = "high";

    onMount(() => {
        $links.displayBackArrow = false;

        const urlSearchParams = new URLSearchParams(window.location.search);
        level = urlSearchParams.get("level") === "medium" ? "medium" : "high";

        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const retry = () => {
        //The institution link already failed to provide the required assurance level, so only offer the external options
        window.location.href = `/stepup/${id}?retry=true`;
    };

    $: levelLabel = I18n.t(level === "medium" ? "IapAssuranceMissing.LevelMedium" : "IapAssuranceMissing.LevelHigh");

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
{#if showSpinner}
    <Spinner/>
{/if}
<div class="home">
    <div class="card">
        <h2>{I18n.t("IapAssuranceMissing.Header")}</h2>
        <p class="info">{I18n.t("IapAssuranceMissing.Info", {level: levelLabel})}</p>
        <p class="info">{I18n.t("IapAssuranceMissing.Proceed", {name: serviceName})}</p>

        <Button href="/proceed" onClick={() => proceed($conf.magicLinkUrl)}
                className="cancel"
                label={I18n.t("Profile.Proceed")}/>
        <div class="last">
            <Button href="/retry" onClick={retry}
                    label={I18n.t("EppnAlreadyLinked.RetryButton")}/>
        </div>

    </div>
</div>
