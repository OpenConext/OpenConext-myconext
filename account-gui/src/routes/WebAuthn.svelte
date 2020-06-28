<script>
    import {create, get, supported} from "@github/webauthn-json";
    import {conf} from "../stores/conf";
    import {onMount} from "svelte";
    import {webAuthnRegistration, webAuthnRegistrationResponse} from "../api";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";
    import I18n from "i18n-js";

    let loading = true;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const token = urlSearchParams.get("token");
        const name = urlSearchParams.get("name");
        webAuthnRegistration(token)
                .then(request => {
                    loading = false;
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
    });

</script>

<style>
    .web-authn {
        display: flex;
        flex-direction: column;
        min-height: 35vh;
    }

    h3 {
        margin: 2vh 0;
    }

    p.info {
        margin: 25px 0;
    }

</style>
<div class="web-authn">
    {#if loading}
        <Spinner/>
    {:else}
        <h3>{I18n.t("webAuthn.info")}</h3>
        <p class="info">{I18n.t("webAuthn.browserPrompt")}</p>
    {/if}
</div>