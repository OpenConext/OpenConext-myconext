<script>
    import {user} from "../stores/user";
    import {validPassword} from "../constants/regexp";
    import I18n from "i18n-js";
    import critical from "../icons/critical.svg";
    import {fetchServiceName, passwordExistingUser} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import Button from "../components/Button.svelte";
    import {cookieNames} from "../constants/cookieNames";
    import {loginPreferences} from "../constants/loginPreferences";
    import {links} from "../stores/conf";

    export let id;
    let showSpinner = true;
    let serviceName = "";
    let passwordIncorrect = false;

    onMount(() => {
        $links.displayBackArrow = true;

        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const passwordStart = () => {
        showSpinner = true;
        passwordExistingUser($user.email, $user.password, id)
            .then(json => {
                Cookies.set(cookieNames.LOGIN_PREFERENCE, loginPreferences.PASSWORD, {
                    expires: 365,
                    secure: true,
                    sameSite: "Lax"
                });
                if (json.stepup) {
                    navigate(`/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${json.explanation}`, {replace: true})
                } else {
                    window.location.href = json.url;
                }
            })
            .catch(e => {
                showSpinner = false;
                if (e.status === 403) {
                    passwordIncorrect = true;
                    $user.password = "";
                } else {
                    navigate("/expired", {replace: true});
                }
            });
    }

    const init = el => el.focus();

    $: allowedNext = validPassword($user.password);

    const handlePasswordEnter = e => e.key === "Enter" && passwordStart();

</script>

<style lang="scss">

    div.error {
        display: flex;
        align-items: center;
        color: var(--color-primary-red);
        margin-bottom: 25px;
    }


    div.error span.svg {
        display: inline-block;
        margin-right: 10px;
    }

    input[type=password] {
        border: 1px solid #727272;
        border-radius: 6px;
        padding: 14px;
        font-size: 16px;
        width: 100%;
        margin: 8px 0 15px 0;
    }

    input.error {
        border: solid 1px var(--color-primary-red);
        background-color: #fff5f3;
    }

    input.error:focus {
        outline: none;
    }


</style>
{#if showSpinner}
    <Spinner/>
{/if}
<h2 class="header">{I18n.t("login.header")}</h2>
<h2 class="top">{I18n.t("login.headerSubTitle")}<span>{serviceName}</span></h2>
<form>
    <input type="email"
           style="display: none"
           autocomplete="username"
           id="email"
           value={$user.email}>

    <input type="password"
           class:error={passwordIncorrect}
           autocomplete="current-password"
           id="password-field"
           placeholder={I18n.t("login.passwordPlaceholder")}
           on:keydown={handlePasswordEnter}
           use:init
           bind:value={$user.password}>
    {#if passwordIncorrect}
        <div class="error">
            <span class="svg">{@html critical}</span>
            <span>{I18n.t("usePassword.passwordIncorrect")}</span>
        </div>
    {/if}
</form>
<Button href="/next"
        disabled={showSpinner || !allowedNext}
        label={I18n.t("login.login")}
        className="full"
        onClick={passwordStart}/>

