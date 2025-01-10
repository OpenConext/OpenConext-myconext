<script>
    import I18n from "../locale/I18n";
    import {onMount} from 'svelte';
    import {conf, links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import {fetchServiceNameByHash, userInfo} from "../api";
    import Spinner from "../components/Spinner.svelte";
    import {proceed} from "../utils/sso";
    import ValidatedData from "../verify/ValidatedData.svelte";

    let serviceName = null;
    let showSpinner = true;
    let institution = null;

    onMount(() => {
        $links.displayBackArrow = false;

        const urlSearchParams = new URLSearchParams(window.location.search);
        const hash = urlSearchParams.get("h");
        Promise.all([fetchServiceNameByHash(hash), userInfo(hash)])
            .then(res => {
                serviceName = res[0].name;
                const user = res[1];
                institution = user.externalLinkedAccounts[0];
                showSpinner = false;

            });

    });

</script>

<style>

</style>
{#if showSpinner}
    <Spinner/>
{:else}
    <ValidatedData institution={institution}/>
    <Button href="/proceed" onClick={() => proceed($conf.magicLinkUrl)}
            className="full"
            label={I18n.t("confirmStepup.proceed", {name: serviceName})}/>
{/if}

