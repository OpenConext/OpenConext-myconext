<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import Button from "../components/Button.svelte";
    import Verification from "../components/Verification.svelte";
    import {fetchServiceName, iDINIssuers} from "../api";
    import Spinner from "../components/Spinner.svelte";
    import {conf, links} from "../stores/conf";
    import DOMPurify from "dompurify";
    import VerifyChoice from "../verify/VerifyChoice.svelte";

    export let id;

    let explanation = null;
    let serviceName = null;
    let showSpinner = true;
    let showChooseOptions = false;
    let issuers = [];
    let isExternalNameValidation = false;

    onMount(() => {
        $links.displayBackArrow = false;
        const urlSearchParams = new URLSearchParams(window.location.search);
        explanation = decodeURIComponent(urlSearchParams.get("explanation"));
        isExternalNameValidation = explanation === "validate_names_external";

        const retry = urlSearchParams.get("retry");
        if (retry) {
            isExternalNameValidation = true;
            showChooseOptions = true;
        }
        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
        if ($conf.featureIdVerify) {
            iDINIssuers().then(res => issuers = res);
        }
    });

    const proceed = () => {
        /*
         * We have several options here. Either the $conf.featureIdVerify is set to False and we
         * right away proceed to the WAYF. Or the requested ACR is https://eduid.nl/trust/validate-names-external
         * and the feature toggle $conf.featureIdVerify is True then we show only the bank chooser or
         * we show all options. There is no need to switch
         */
        if (!$conf.featureIdVerify) {
            //proceed to WAYF right away
            window.location.href = `/myconext/api/idp/oidc/account/${id}`;
        } else {
            showChooseOptions = true;
        }
    };

    const addInstitution = () => {
        //proceed to WAYF right away
        window.location.href = `/myconext/api/idp/oidc/account/${id}`;
    }

    const addBank = bankId => {
        const bankIdParam = `&bankId=${bankId}`;
        window.location.href = `/myconext/api/idp/verify/link/${id}?idpScoping=idin${bankIdParam}`;
    }

    const addEuropean = () => {
        window.location.href = `/myconext/api/idp/verify/link/${id}?idpScoping=eherkenning`;
    }

</script>

<style>
    h2 {
        margin: 0 0 30px  0;
        font-size: 32px;
        color: var(--color-primary-green);
    }

    p.info {
        margin-bottom: 30px;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<div class="home">
    <div class="card">
        {#if showChooseOptions}
            <VerifyChoice addBank={addBank}
                          addEuropean={addEuropean}
                          addInstitution={addInstitution}
                          issuers={issuers}
                          id={id}
                          showInstitutionOption={!isExternalNameValidation}
                          serviceName={serviceName}
            />
        {:else}
            <h2>{I18n.t("stepup.header")}</h2>
            <p class="info">{@html I18n.t("stepup.info", {name: DOMPurify.sanitize(serviceName)})}</p>
            <Verification explanation={explanation} verified={false}/>
            <Button href="/proceed" onClick={() => proceed(false)}
                    className="full"
                    label={I18n.t("stepup.link")}/>
        {/if}
    </div>
</div>
