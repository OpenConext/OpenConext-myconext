<script>
    import {get} from "@github/webauthn-json";
    import {onMount} from "svelte";
    import {
        webAuthnStartAuthentication,
        webAuthnTryAuthentication
    } from "../api";
    import Spinner from "../components/Spinner.svelte";
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";
    import {user} from "../stores/user";
    import Cookies from "js-cookie";

    export let id;
    let email;
    let showSpinner = true;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        email = urlSearchParams.get("email")
        showSpinner = false;
    });

    const webAuthnStart = () => {
        webAuthnStartAuthentication(email, id, true)
            .then(request => {
                get({publicKey: request.publicKeyCredentialRequestOptions})
                    .then(credentials => {
                        //rawId is not supported server-side
                        delete credentials["rawId"];
                        webAuthnTryAuthentication(JSON.stringify(credentials), id, $user.rememberMe)
                            .then(json => {
                                Cookies.set("login_preference", "useWebAuth", {
                                    expires: 365,
                                    secure: true,
                                    sameSite: "Lax"
                                });
                                window.location.href = json.url
                            })
                    })
                })
    }

</script>

<style>
    .web-authn-test {
        display: flex;
        flex-direction: column;
        min-height: 35vh;
    }

    p.info {
        margin: 25px 0;
    }

</style>
<div class="web-authn-test">
    {#if showSpinner}
        <Spinner/>
    {:else}
        <h2>{I18n.t("webAuthnTest.info")}</h2>
        <p class="info">{I18n.t("webAuthnTest.browserPrompt")}</p>
        <Button label={I18n.t("webAuthnTest.start")} onClick={webAuthnStart}/>
    {/if}
</div>