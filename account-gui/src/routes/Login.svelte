<script>
    import {user} from "../stores/user";
    import {links} from "../stores/conf";
    import {validEmail} from "../constants/regexp";
    import I18n from "i18n-js";
    import critical from "../icons/critical.svg";
    import {link, navigate} from "svelte-routing";
    import {fetchServiceName, knownAccount} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import Button from "../components/Button.svelte";
    import userIcon from "../icons/redesign/single-neutral-id-card-1.svg";
    import accountIcon from "../icons/redesign/single-neutral-actions-refresh.svg";
    import {cookieNames} from "../constants/cookieNames";
    import LoginOption from "../components/LoginOption.svelte";

    export let id;
    let emailNotFound = false;
    let showSpinner = true;
    let serviceName = "";

    onMount(() => {
        $links.userLink = false;
        $links.displayBackArrow = false;
        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
        const urlParams = new URLSearchParams(window.location.search);
        const modus = urlParams.get("modus");
        const registerModus = Cookies.get("REGISTER_MODUS");
        if ((modus && modus === "cr") || registerModus) {
            navigate("/request");
        }
    });

    const init = el => el.focus();

    const allowedNext = email => validEmail(email) || $user.knownUser;

    const nextStep = () => {
        knownAccount($user.knownUser || $user.email)
            .then(res => {
                $user.knownUser = $user.email;
                $links.userLink = true;
                Cookies.set(cookieNames.USERNAME, $user.email, {
                    expires: 365,
                    secure: true,
                    sameSite: "Lax"
                });
                //If the server does not confirm the preferredLogin, we won't use it
                if ($user.preferredLogin && res.includes($user.preferredLogin)) {
                    navigate(`/${$user.preferredLogin.toLowerCase()}/${id}`);
                } else {
                    navigate(`/${res[0].toLowerCase()}/${id}`);
                }
            }).catch(() => emailNotFound = true);

    }

    const handleEmailEnter = e => {
        if (e.key === "Enter" && allowedNext(e.target.value)) {
            nextStep();
        }
    };

    const otherAccount = () => {
        Cookies.remove(cookieNames.USERNAME);
        $user.knownUser = null;
        $user.email = "";
    }

</script>

<style lang="scss">

    :global(span.svg.attention svg) {
        width: 32px;
        height: 32px;
        margin: 5px;
    }

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

    input[type=email] {
        border: 1px solid #727272;
        border-radius: 2px;
        padding: 14px;
        font-size: 16px;
        width: 100%;
        margin-bottom: 15px;
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
{#if $user.knownUser}
    <div class="known-user">
        <LoginOption icon={userIcon} label={$user.knownUser} action={nextStep} index={1} preferred={true}/>
    </div>
    <div class="other-account">
        <LoginOption icon={accountIcon} label={I18n.t("login.useOtherAccount")} action={otherAccount} index={2}/>
    </div>
{:else}
    <input type="email"
           autocomplete="username"
           id="email"
           class={`${emailNotFound ? 'error' : ''}`}
           placeholder={I18n.t("login.emailPlaceholder")}
           use:init
           bind:value={$user.email}
           on:keydown={handleEmailEnter}>
{/if}
{#if emailNotFound}
    <div class="error">
        <span class="svg">{@html critical}</span>
        <div>
            <span>{I18n.t("login.emailNotFound1")}</span>
            <span>{I18n.t("login.emailNotFound2")}</span>
            <a href={`/request/${id}`}
               use:link>{I18n.t("login.emailNotFound3")}</a>
        </div>
    </div>
{/if}

<Button href="/next"
        disabled={showSpinner || !allowedNext($user.email)}
        label={I18n.t("login.next")}
        className="full"
        onClick={nextStep}/>

