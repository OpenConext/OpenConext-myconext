<script>
    import {user} from "../stores/user";
    import I18n from "../locale/I18n";

    import {codeNewUser, fetchServiceName} from "../api/index";
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
    let code = false;
    let mrccValue = null;
    let codeSend = false;

    onMount(() => {
        const urlParams = new URLSearchParams(window.location.search);
        mrccValue = urlParams.get(mrcc);
        code = urlParams.has("code");

        if (mrccValue) {
            codeStart();
        } else {
            $links.displayBackArrow = true;
            fetchServiceName(id).then(res => {
                serviceName = res.name;
                showSpinner = false;
            });
        }
    });

    const codeStart = () => {
        showSpinner = true;
        codeNewUser($user.email, id)
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
                codeSend = true;
            }).catch(() => navigate("/expired", {replace: true}));
    };

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
        {#if mrccValue}
            <p>{I18n.t("login.sendingEmail")}</p>
        {/if}
    </div>
{/if}
{#if !showSpinner && !mrccValue && !codeSend}
    <h2 class="header">{I18n.t("UseLink.Header.COPY")}</h2>
    {#if serviceName}
        <h2 class="top">{I18n.t("Login.HeaderSubTitle.COPY")}<span>{serviceName}</span></h2>
    {/if}
    <Button href="/start"
            disabled={showSpinner}
            label={I18n.t("Login.SendCode.COPY")}
            className="full"
            onClick={codeStart}/>
{/if}
{#if !showSpinner && codeSend}
    <h2 class="header">{I18n.t("UseLink.Header.COPY")}</h2>
    {#if serviceName}
        <h2 class="top">{I18n.t("Login.HeaderSubTitle.COPY")}<span>{serviceName}</span></h2>
    {/if}
    <Button href="/start"
            disabled={showSpinner}
            label={I18n.t("Login.SendCode.COPY")}
            className="full"
            onClick={codeStart}/>
{/if}