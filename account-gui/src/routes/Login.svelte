<script>
    import {user} from "../stores/user";
    import {links} from "../stores/conf";
    import {validEmail} from "../constants/regexp";
    import I18n from "../locale/I18n";
    import critical from "../icons/critical.svg?raw";
    import {link, navigate} from "svelte-routing";
    import {fetchServiceName, knownAccount} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import Button from "../components/Button.svelte";
    import userIcon from "../icons/redesign/single-neutral-id-card-1.svg?raw";
    import accountIcon from "../icons/redesign/single-neutral-actions-refresh.svg?raw";
    import {cookieNames} from "../constants/cookieNames";
    import LoginOption from "../components/LoginOption.svelte";
    import {loginPreferences} from "../constants/loginPreferences";
    import SubContent from "../components/SubContent.svelte";
    import Alert from "../components/Alert.svelte";
    import AlertType from "../constants/AlertType.js";

    export let id;
    let mfaRequired = false;
    let magicLink = false;
    let emailNotFound = false;
    let rateLimited = false;
    let minutesDelay = false;
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
        mfaRequired = urlParams.has("mfa");
        magicLink = urlParams.has("magicLink")
        if (modus && modus === "cr") {
            navigate(`/request/${id}`);
        }
    });

    const init = el => el.focus();

    const handleInput = e => {
        const email = (e.target.value || "").replace(/[<>]/g, "");
        e.target.value = email;
        $user.email = email;
    }

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
                if (magicLink) {
                    navigate(`/${loginPreferences.CODE.toLowerCase()}/${id}`);
                } else if (mfaRequired && res.includes(loginPreferences.APP)) {
                    navigate(`/${loginPreferences.APP.toLowerCase()}/${id}?mfa=true`);
                }
                //If the server does not confirm the preferredLogin, we won't use it
                else if ($user.preferredLogin && res.includes($user.preferredLogin)) {
                    navigate(`/${$user.preferredLogin.toLowerCase()}/${id}`);
                } else {
                    //By contract the list is ordered from more secure to less secure
                    navigate(`/${res[0].toLowerCase()}/${id}`);
                }
            }).catch(e => {
            if (e.status === 409) {
                e.json().then(res => {
                    rateLimited = true;
                    minutesDelay = 15 - Math.floor((new Date().getTime() - parseInt(res.message)) / 1000 / 60);
                })
            } else {
                emailNotFound = true
            }
        });

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
        rateLimited = false;
        emailNotFound = false;
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
<h2 class="header">{I18n.t("Login.Header.COPY")}</h2>
{#if serviceName}
    <h2 class="top">{I18n.t("Login.HeaderSubTitle.COPY")}<span>{serviceName}</span></h2>
{/if}
{#if $user.knownUser}
    <div class="known-user">
        <LoginOption icon={userIcon}
                     label={$user.knownUser}
                     action={nextStep}
                     index={1}
                     error={rateLimited}
                     preferred={true}/>
    </div>
    <div class="other-account">
        <LoginOption icon={accountIcon}
                     label={I18n.t("Login.UseOtherAccount.COPY")}
                     action={otherAccount}
                     index={2}/>
    </div>
{:else}
    <input type="email"
           autocomplete="username"
           id="email"
           class={`${(rateLimited) ? 'error' : ''}`}
           placeholder={I18n.t("LinkFromInstitution.EmailPlaceholder.COPY")}
           use:init
           on:input={handleInput}
           value={$user.email}
           on:keydown={handleEmailEnter}
           spellcheck="false">
{/if}
{#if emailNotFound}
    <Alert
            message={I18n.t("Login.EmailNotFound1.COPY")}
            alertType={AlertType.Warning}
    />
{/if}
{#if rateLimited}
    <div class="error">
        <span class="svg">{@html critical}</span>
        <div>
            <span>{I18n.t("Login.RateLimited.COPY", {minutes: minutesDelay})}</span>
        </div>
    </div>
{/if}
{#if emailNotFound}
    <SubContent linkText={I18n.t("Login.RequestEduId2.COPY")}
                route="/request/{id}"
                interContent="true"
                showButton="true"
    />

    <div style="width: 100%"><span style="width: 100%;height: 0px;border: 1px solid #000000;"></span>of<span style="height: 0px;border: 1px solid #000000;"></span></div>

    <Button href="/"
            label={I18n.t("Login.TryOtherEmail.COPY")}
            className="cancel"
            onClick={otherAccount}/>
{:else}
<Button href="/next"
        disabled={showSpinner || !allowedNext($user.email)}
        label={I18n.t("GetApp.Next.COPY")}
        className="full"
        onClick={nextStep}/>

<SubContent question={I18n.t("Login.RequestEduId.COPY")}
            linkText={I18n.t("Login.RequestEduId2.COPY")}
            route="/request/{id}"
            interContent="true"
/>
{/if}