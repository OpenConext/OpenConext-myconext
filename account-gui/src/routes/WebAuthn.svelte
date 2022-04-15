<script>
    import {create, get, supported} from "@github/webauthn-json";
    import {conf, links} from "../stores/conf";
    import {onMount} from "svelte";
    import {webAuthnRegistration, webAuthnRegistrationResponse} from "../api";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";

    let showSpinner = true;
    let token;
    let name;

    onMount(() => {
        $links.displayBackArrow = true;

        const urlSearchParams = new URLSearchParams(window.location.search);
        token = urlSearchParams.get("token");
        name = decodeURIComponent(urlSearchParams.get("name"));
        showSpinner = false;
    });

    const startWebAuthnRegistration = () => {
        showSpinner = true;
        webAuthnRegistration(token)
            .then(request => {
                showSpinner = false;
                create({publicKey: request})
                    .then(credentials => {
                        //rawId is not supported server-side
                        delete credentials["rawId"];
                        webAuthnRegistrationResponse(token, name, JSON.stringify(credentials), request)
                            .then(res => window.location.href = res.location);
                    })
                    .catch(() => {
                        //happens when the key is already registered
                        window.location.href = $conf.eduIDWebAuthnRedirectSpUrl;
                    })
            })
            .catch(() => navigate("/404"));
    };

</script>

<style>
    .web-authn {
        display: flex;
        flex-direction: column;
        min-height: 35vh;
    }

    p.info {
        margin: 25px 0;
    }

</style>
<div class="web-authn">
    {#if showSpinner}
        <Spinner/>
    {:else}
        <h2>{I18n.t("webAuthn.info")}</h2>
        <p class="info">{I18n.t("webAuthn.browserPrompt")}</p>
        <Button label={I18n.t("webAuthn.start")} onClick={startWebAuthnRegistration}/>
    {/if}
</div>