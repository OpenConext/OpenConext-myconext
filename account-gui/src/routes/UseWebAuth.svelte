<script>
    import {user} from "../stores/user";
    import {navigate} from "svelte-routing";
    import {get} from "@github/webauthn-json";
    import I18n from "i18n-js";
    import {fetchServiceName, webAuthnStartAuthentication, webAuthnTryAuthentication} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import Button from "../components/Button.svelte";
    import {cookieNames} from "../constants/cookieNames";
    import {loginPreferences} from "../constants/loginPreferences";
    import {links} from "../stores/conf";

    export let id;
    let showSpinner = true;
    let serviceName = "";

    onMount(() => {
        $links.displayBackArrow = true;

        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const webAuthnStart = (test = false) => {
        webAuthnStartAuthentication($user.email, id, test)
            .then(request => {
                get({publicKey: request.publicKeyCredentialRequestOptions})
                    .then(credentials => {
                        //rawId is not supported server-side
                        delete credentials["rawId"];
                        webAuthnTryAuthentication(JSON.stringify(credentials), id, $user.rememberMe)
                            .then(json => {
                                Cookies.set(cookieNames.LOGIN_PREFERENCE, loginPreferences.FIDO, {
                                    expires: 365,
                                    secure: true,
                                    sameSite: "Lax"
                                });
                                if (json.stepup) {
                                    navigate(`/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${json.explanation}`, {replace: true})
                                } else {
                                    window.location.href = json.url
                                }
                            })

                    })
            })
    }


</script>

<style lang="scss">


</style>

{#if showSpinner}
    <Spinner/>
{/if}
<h2 class="header">{I18n.t("webAuthn.header")}</h2>
<h2 class="top">{I18n.t("login.headerSubTitle")}<span>{serviceName}</span></h2>
<p class="explanation">{I18n.t("webAuthn.explanation")}</p>
<Button href="/start"
        disabled={showSpinner}
        label={I18n.t("webAuthn.next")}
        className="full"
        onClick={webAuthnStart}/>


