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

    export let id;
    let showSpinner = true;
    let serviceName = "";

    onMount(() => {
        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const magicLinkStart = () => {
        showSpinner = true;
        magicLinkExistingUser($user.email, id)
            .then(json => {
                Cookies.set(cookieNames.LOGIN_PREFERENCE, loginPreferences.MAGIC, {
                    expires: 365,
                    secure: true,
                    sameSite: "Lax"
                });
                if (json.stepup) {
                    navigate(`/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${json.explanation}`, {replace: true})
                } else {
                    navigate(`/magic/${id}?name=${encodeURIComponent(serviceName)}`, {replace: true});
                }
            }).catch(() => navigate("/expired", {replace: true}));
    };

</script>

<style lang="scss">


</style>
{#if showSpinner}
    <Spinner/>
{/if}
<h2 class="header">{I18n.t("useLink.header")}</h2>
<h2 class="top">{I18n.t("login.headerSubTitle")}<span>{serviceName}</span></h2>
<Button href="/start"
        disabled={showSpinner}
        label={I18n.t("useLink.next")}
        className="full"
        onClick={magicLinkStart}/>
