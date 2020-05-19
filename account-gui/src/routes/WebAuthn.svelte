<script>
    import {create, get, supported} from "@github/webauthn-json"
    import {onMount} from "svelte";
    import {webAuthnRegistration, webAuthnRegistrationResponse} from "../api";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";
    import I18n from "i18n-js";

    let loading = true;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const token = urlSearchParams.get("token");
        webAuthnRegistration(token)
                .then(res => {
                    loading = false;
                    create({publicKey: res}).then(credentials => {
                        delete credentials["rawId"];
                        webAuthnRegistrationResponse(token, JSON.stringify(credentials))
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

    p.info {
        margin: 15vh 0;
    }

</style>
<div class="web-authn">
    {#if loading}
        <Spinner/>
    {:else}
        <p class="info">{I18n.t("webAuthn.info")}</p>
    {/if}
</div>