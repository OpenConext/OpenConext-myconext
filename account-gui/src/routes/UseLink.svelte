<script>
    import {user} from "../stores/user";
    import I18n from "i18n-js";

    import {fetchServiceName, magicLinkExistingUser} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import Button from "../components/Button.svelte";
    import {loginPreferences} from "../constants/loginPreferences";
    import {cookieNames} from "../constants/cookieNames";
    import {links} from "../stores/conf";
    import {mrcc} from "../utils/constants";

    export let id;
    let showSpinner = true;
    let serviceName = "";
    let magicLink = false;
    let mrccValue = null;

    onMount(() => {
        const urlParams = new URLSearchParams(window.location.search);
        mrccValue = urlParams.get(mrcc);
        magicLink = urlParams.has("magicLink");

        if (mrccValue) {
            magicLinkStart();
        } else {
            $links.displayBackArrow = true;
            fetchServiceName(id).then(res => {
                serviceName = res.name;
                showSpinner = false;
            });
        }
    });

    const magicLinkStart = () => {
        showSpinner = true;
        magicLinkExistingUser($user.email, id)
            .then(json => {
                if (!magicLink) {
                    Cookies.set(cookieNames.LOGIN_PREFERENCE, loginPreferences.MAGIC, {
                        expires: 365,
                        secure: true,
                        sameSite: "Lax"
                    });
                }
                if (json.stepup) {
                    navigate(`/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${json.explanation}`, {replace: true})
                } else {
                    navigate(`/magic/${id}?name=${encodeURIComponent(serviceName)}`, {replace: true});
                }
            }).catch(() => navigate("/expired", {replace: true}));
    };

</script>
{#if showSpinner}
    <Spinner/>
{/if}
{#if !showSpinner && !mrccValue}
    <h2 class="header">{I18n.t("useLink.header")}</h2>
    {#if serviceName}
        <h2 class="top">{I18n.t("login.headerSubTitle")}<span>{serviceName}</span></h2>
    {/if}
    <Button href="/start"
            disabled={showSpinner}
            label={I18n.t("useLink.next")}
            className="full"
            onClick={magicLinkStart}/>
{/if}