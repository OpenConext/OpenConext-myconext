<script>
    import {user} from "../stores/user";
    import {conf} from "../stores/conf";

    import {get} from "@github/webauthn-json";
    import {validEmail} from "../constants/regexp";
    import I18n from "i18n-js";
    import critical from "../icons/critical.svg";
    import attention from "../icons/alert-circle.svg";

    import {
        fetchServiceName,
        magicLinkExistingUser,
        magicLinkNewUser,
        webAuthnStartAuthentication,
        webAuthnTryAuthentication
    } from "../api/index";
    import CheckBox from "../components/CheckBox.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import Button from "../components/Button.svelte";
    import {domains} from "../stores/domains";

    export let id;
    let emailInUse = false;
    let emailNotFound = false;
    let emailOrPasswordIncorrect = false;
    let webAuthIncorrect = false;
    let initial = true;
    let showSpinner = true;
    let serviceName = "";

    let passwordField;
    let agreedWithTerms = false;

    const intervalId = setInterval(() => {
        const value = (passwordField || {}).value;
        if (value && !$user.usePassword) {
            $user.usePassword = true;
            clearInterval(intervalId);
        }
    }, 750);

    onMount(() => {
        $user.email = "";
        $user.givenName = "";
        $user.familyName = "";
        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
        const value = Cookies.get("login_preference");
        $user.usePassword = value === "usePassword";
        $user.useWebAuth = value === "useWebAuth" && $conf.featureWebAuthn;
    });

    const handleNext = passwordFlow => () => {
        if (($user.usePassword && passwordFlow) || (!$user.usePassword && !passwordFlow)) {
            if (allowedNext($user.email, $user.givenName, $user.familyName, $user.password, agreedWithTerms)) {
                showSpinner = true;
                const modus = $user.createAccount ? "cr" : "ea";
                if ($user.createAccount) {
                    magicLinkNewUser($user.email, $user.givenName, $user.familyName, $user.rememberMe, id)
                        .then(res => {
                            const url = res.stepup ? `/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${res.explanation}` :
                                `/magic/${id}?name=${encodeURIComponent(serviceName)}&modus=${modus}`;
                            navigate(url, {replace: true})
                        })
                        .catch(e => {
                            showSpinner = false;
                            if (e.status === 409) {
                                emailInUse = true;
                                emailNotFound = false;
                                emailOrPasswordIncorrect = false;
                            } else {
                                navigate("/expired", {replace: true});
                            }
                        });
                } else {
                    magicLinkExistingUser($user.email, $user.password, $user.rememberMe, $user.usePassword, id)
                        .then(json => {
                            Cookies.set("login_preference", $user.usePassword ? "usePassword" : $user.useWebAuth ? "useWebAuth" : "useLink", {
                                expires: 365,
                                secure: true,
                                sameSite: "Lax"
                            });
                            if (json.stepup) {
                                navigate(`/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${json.explanation}`, {replace: true})
                            } else if ($user.usePassword) {
                                window.location.href = json.url;
                            } else {
                                navigate(`/magic/${id}?name=${encodeURIComponent(serviceName)}&modus=${modus}`, {replace: true});
                            }
                        }).catch(e => {
                        showSpinner = false;
                        if (e.status === 404) {
                            if ($user.usePassword) {
                                emailNotFound = false;
                                emailOrPasswordIncorrect = true;
                                emailInUse = false;
                            } else {
                                emailNotFound = true;
                                emailOrPasswordIncorrect = false;
                                emailInUse = false;
                            }
                        } else if (e.status === 403) {
                            emailNotFound = false;
                            emailOrPasswordIncorrect = true;
                            emailInUse = false;

                        } else {
                            navigate("/expired", {replace: true});
                        }
                    });
                }
            } else {
                initial = false;
            }
        } else {
            $user.usePassword = passwordFlow;
            if (!passwordFlow) {
                $user.password = "";
            }
        }
    };

    const init = el => el.focus();

    const allowedNext = (email, familyName, givenName, password, agreedWithTerms) => {
        return $user.createAccount ? validEmail(email) && familyName && givenName && agreedWithTerms && !$domains.allowedDomainNamesError :
            $user.usePassword ? validEmail(email) && password : validEmail(email);
    };

    const webAuthnStart = (email, test = false) => {
        webAuthnStartAuthentication(email, id, test)
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
                                if (json.stepup) {
                                    navigate(`/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${json.explanation}`, {replace: true})
                                } else {
                                    window.location.href = json.url
                                }
                            })
                            .catch(() => {
                                webAuthIncorrect = true;
                            })
                    }).catch(e => {
                    webAuthIncorrect = true;
                });
            })
            .catch(e => {
                if (e.status === 404) {
                    emailNotFound = true;
                    emailOrPasswordIncorrect = false;
                    emailInUse = false;
                }
            })
    }

    const createAccount = newAccount => () => {
        $user.createAccount = newAccount;
        $user.usePassword = false;
        $user.givenName = "";
        $user.familyName = "";
        emailInUse = false;
        emailNotFound = false;
        if (newAccount) {
            fetchDomainsForValidation(() => handleEmailBlur({target: {value: $user.email}}));
        } else {
            handleEmailBlur({target: {value: $user.email}});
        }
    };

    const handleEmailEnter = e => {
        if (e.key === "Enter") {
            if (!$user.createAccount && !$user.usePassword && !$user.useWebAuth) {
                handleNext(false)();
            } else if ($user.usePassword) {
                passwordField.focus();
            } else if ($user.useWebAuth) {
                webAuthnStart($user.email)
            }
        }
    };

    const handleEmailBlur = e => {
        if ($user.createAccount) {
            const email = e.target.value;
            //defensive, in edge-cases we don't validate
            if (email && email.lastIndexOf("@") > -1) {
                const domain = email.substring(email.lastIndexOf("@") + 1);
                if (domain) {
                    const domainLower = domain.toLowerCase();
                    if ($conf.featureAllowList && !$domains.allowedDomainNames.some(name => name === domainLower || domainLower.endsWith("." + name))) {
                        $domains.allowedDomainNamesError = true;
                        return;
                    }
                    if ($conf.featureWarningEducationalEmailDomain && $domains.institutionDomainNames.some(name => name === domainLower || domainLower.endsWith("." + name))) {
                        $domains.institutionDomainNameWarning = true;
                        return;
                    }
                }
            }
        }
        $domains.institutionDomainNameWarning = false;
        $domains.allowedDomainNamesError = false;
    }

    const handlePasswordEnter = e => e.key === "Enter" && handleNext(true)();

    const clearGivenName = () => $user.givenName = $user.givenName.replace(/[<>]/g, "");
    const clearFamilyName = () => $user.familyName = $user.familyName.replace(/[<>]/g, "");

    const switchWebAuthnPassword = (webAuth, password) => () => {
        $user.useWebAuth = webAuth;
        $user.usePassword = password;
        if (!password) {
            $user.password = "";
        }
    }

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
        padding-top: 5px;
    }

    div.error {
        display: flex;
        align-items: center;
        color: var(--color-primary-red);
        margin-bottom: 25px;
    }

    div.institution-warning {
        border-radius: 8px;
        background-color: #ffd89d;
        display: flex;
        padding: 6px;
        margin-bottom: 15px;
    }

    div.domain-not-allowed {
        border-radius: 8px;
        background-color: #ffc5c1;
        display: flex;
        padding: 6px;
        margin-bottom: 15px;
    }

    div.text {
        padding: 5px 5px 5px 10px;
    }

    div.error span.svg {
        display: inline-block;
        margin-right: 10px;
    }

    span.error {
        display: inline-block;
        margin: 0 auto 10px 0;
        color: var(--color-primary-red);
    }

    input[type=email], input[type=text] {
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
<h2 class="header">{I18n.t("login.header2")}</h2>
<h2 class="top">{I18n.t("login.headerSubTitle")}<span>{serviceName}</span></h2>
<label for="email" class="pre-input-label">{I18n.t("login.email")}</label>
<input type="email"
       autocomplete="username"
       id="email"
       class={`${emailNotFound || emailOrPasswordIncorrect || emailInUse ? 'error' : ''}`}
       placeholder={I18n.t("login.emailPlaceholder")}
       use:init
       bind:value={$user.email}
       on:blur={handleEmailBlur}
       on:keydown={handleEmailEnter}>
{#if emailNotFound}
    <div class="error">
        <span class="svg">{@html critical}</span>
        <div>
            <span>{I18n.t("login.emailNotFound1")}</span>
            <span>{I18n.t("login.emailNotFound2")}</span>
            <a href="/reguest"
               on:click|preventDefault|stopPropagation={createAccount(true)}>{I18n.t("login.emailNotFound3")}</a>
        </div>
    </div>
{/if}
{#if $user.usePassword && emailOrPasswordIncorrect}
    <div class="error"><span class="svg">{@html critical}</span>
        <span>{I18n.t("login.emailOrPasswordIncorrect")}</span>
    </div>
{/if}
{#if !initial && !validEmail($user.email)}
    <div class="error"><span class="svg">{@html critical}</span><span>{I18n.t("login.invalidEmail")}</span></div>
{/if}
{#if emailInUse}
    <div class="error">
        <span class="svg">{@html critical}</span>
        <div>
            <span>{I18n.t("login.emailInUse1")}</span>
            <span>{I18n.t("login.emailInUse2")}</span>
            <a href="/reguest"
               on:click|preventDefault|stopPropagation={createAccount(false)}>{I18n.t("login.emailInUse3")}</a>
        </div>
    </div>
{/if}
{#if $domains.institutionDomainNameWarning}
    <div class="institution-warning">
        <span class="svg attention">{@html attention}</span>
        <div class="text">
            <span>{I18n.t("login.institutionDomainNameWarning")}</span>
            <br/>
            <span>{I18n.t("login.institutionDomainNameWarning2")}</span>
        </div>
    </div>

{/if}

{#if $domains.allowedDomainNamesError}
    <div class="domain-not-allowed">
        <span class="svg error">{@html critical}</span>
        <div class="text">
            <span>{I18n.t("login.allowedDomainNamesError",
                {domain: $user.email.substring($user.email.indexOf("@") + 1)})}</span>
            <br/>
            <span>{I18n.t("login.allowedDomainNamesError2")}</span>
        </div>
    </div>

{/if}

<label for="given-name" class="pre-input-label">{I18n.t("login.givenName")}</label>
<input type="text"
       id="given-name"
       placeholder={I18n.t("login.givenNamePlaceholder")}
       bind:value={$user.givenName}
       on:change={clearGivenName}>
{#if !initial && !$user.givenName}
    <span class="error">{I18n.t("login.requiredAttribute", {attr: I18n.t("login.givenName")})}</span>
{/if}
<label for="family-name" class="pre-input-label">{I18n.t("login.familyName")}</label>
<input type="text"
       id="family-name"
       placeholder={I18n.t("login.familyNamePlaceholder")}
       bind:value={$user.familyName}
       on:change={clearFamilyName}>
{#if !initial && !$user.familyName}
    <span class="error">{I18n.t("login.requiredAttribute", {attr: I18n.t("login.familyName")})}</span>
{/if}
<CheckBox value={agreedWithTerms}
          className="light"
          label={I18n.t("login.agreeWithTerms")}
          onChange={val => agreedWithTerms = val}/>
<div class="options">
    <Button disabled={showSpinner || !allowedNext($user.email, $user.familyName, $user.givenName, $user.password, agreedWithTerms)}
            href="/magic"
            className="full"
            label={I18n.t("login.requestEduIdButton")}
            onClick={handleNext(false)}/>
</div>