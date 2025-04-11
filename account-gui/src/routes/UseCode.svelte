<script>
    import {user} from "../stores/user";
    import I18n from "../locale/I18n";

    import {generateCodeExistingUser} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import {loginPreferences} from "../constants/loginPreferences";
    import {cookieNames} from "../constants/cookieNames";

    export let id;
    let showSpinner = true;
    let serviceName = "";
    let code = false;

    onMount(() => {
        generateCodeExistingUser($user.email, id)
            .then(json => {
                if (!code) {
                    Cookies.set(cookieNames.LOGIN_PREFERENCE, loginPreferences.CODE, {
                        expires: 365,
                        secure: true,
                        sameSite: "Lax"
                    });
                }
                if (json.stepup) {
                    navigate(`/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${json.explanation}`, {replace: true})
                } else {
                    navigate(`/code/${id}?name=${encodeURIComponent(serviceName)}`, {replace: true});
                }
            }).catch(() => navigate("/expired", {replace: true}));
    });

</script>
<style lang="scss">
    div.spinner-container {
        display: flex;
        flex-direction: column;

        p {
            margin: 75px auto 0 auto;
        }
    }
</style>
{#if showSpinner}
    <div class="spinner-container">
        <Spinner/>
        <p>{I18n.t("login.sendingEmail")}</p>
    </div>
{/if}
