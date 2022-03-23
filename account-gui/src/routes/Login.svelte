<script>
    import {user} from "../stores/user";
    import {validEmail} from "../constants/regexp";
    import I18n from "i18n-js";
    import critical from "../icons/critical.svg";
    import {link, navigate} from "svelte-routing";
    import {fetchServiceName, knownAccount} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import Button from "../components/Button.svelte";
    import userIcon from "../icons/video-game-magic-wand.svg";

    export let id;
    let emailNotFound = false;
    let initial = true;
    let showSpinner = true;
    let serviceName = "";
    let passwordField;

    onMount(() => {
        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
        const urlParams = new URLSearchParams(window.location.search);
        const modus = urlParams.get("modus");
        const registerModus = Cookies.get("REGISTER_MODUS");
        if ((modus && modus === "cr") || registerModus) {
            navigate("/request")
        }
    });


    const init = el => el.focus();

    const allowedNext = email => validEmail(email) || $user.knownUser;

    const nextStep = () => {
        knownAccount($user.email)
            .then(res => {
                if ($user.preferredLogin) {
                    navigate(`/${$user.preferredLogin.toLowerCase()}/${id}`);
                } else {
                    navigate(`/${res[0].toLowerCase()}/${id}`)
                }
            }).catch(() => emailNotFound = true);

    }

    const handleEmailEnter = e => {
        if (e.key === "Enter" && allowedNext(e.target.value)) {
            nextStep();
        }
    };

</script>

<style lang="scss">

    :global(span.svg.attention svg) {
        width: 32px;
        height: 32px;
        margin: 5px;
    }

    h2.header {
        font-size: 24px;
    }

    h2.top {
        margin: 0 0 15px 0;
        word-break: break-word;
        font-size: 16px;
        font-weight: 600;

        span {
            color: var(--color-primary-green);
            font-size: 16px;
            font-family: Nunito, sans-serif;

        }
    }

    .options {
        display: flex;
        flex-direction: column;
        width: 100%;
        padding: 5px 0 30px 0;
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

    div.known-user {
        display: flex;
        align-items: center;
        border-radius: 2px;
        border: 1px solid #979797;
        background-color: #f3f6f7;
        margin-bottom: 8px;

        span.icon {
            background-color: #dee3e7;
            border-right: 1px solid #979797;
            padding: 4px;
        }

        span.user-name {
            padding-left: 15px;
        }

    }

    a.other-account {
        margin-bottom: 15px;
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

    .hidden {
        display: none;
    }

    .pre-input-label {
        color: var(--color-primary-black);
        font-weight: 600;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<h2 class="header">{I18n.t("login.header")}</h2>
<h2 class="top">{I18n.t("login.headerSubTitle")}<span>{serviceName}</span></h2>
{#if $user.knownUser}
    <div class="known-user">
        <span class="icon">{@html userIcon}</span>
        <span class="user-name">{$user.knownUser}</span>
    </div>
    <a class="other-account"
       href="/account"
       on:click|preventDefault|stopPropagation={() => $user.knownUser = null}>
        {I18n.t("login.useOtherAccount")}
    </a>
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

{#if !initial && !validEmail($user.email)}
    <div class="error"><span class="svg">{@html critical}</span><span>{I18n.t("login.invalidEmail")}</span></div>
{/if}
<div id="password" class:hidden={!$user.usePassword || $user.useWebAuth}>
    <input type="password"
           autocomplete="current-password"
           id="password-field"
           placeholder={I18n.t("login.passwordPlaceholder")}
           bind:value={$user.password}
           bind:this={passwordField}>
</div>

<Button href="/magic"
        disabled={showSpinner || !allowedNext($user.email)}
        label={I18n.t("login.next")}
        className="full"
        onClick={nextStep}/>

