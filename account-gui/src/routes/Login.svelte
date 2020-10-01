<script>
  import {user} from "../stores/user";
  import {conf} from "../stores/conf";

  import {get} from "@github/webauthn-json";
  import {validEmail} from "../validation/regexp";
  import I18n from "i18n-js";
  import critical from "../icons/critical.svg";
  import attention from "../icons/attention.svg";

  import {
    domainNames,
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
  import LoginOptions from "../components/LoginOptions.svelte";

  export let id;
  let emailInUse = false;
  let emailNotFound = false;
  let emailOrPasswordIncorrect = false;
  let webAuthIncorrect = false;
  let initial = true;
  let showSpinner = false;
  let serviceName = "";

  let passwordField;
  let agreedWithTerms = false;

  let institutionDomainNames = [];
  let institutionDomainNameWarning = false;

  const intervalId = setInterval(() => {
    const value = (passwordField || {}).value;
    if (value && !$user.usePassword) {
      $user.usePassword = true;
      clearInterval(intervalId);
    }
  }, 750);

  onMount(() => {
    const value = Cookies.get("login_preference");
    $user.usePassword = value === "usePassword";
    $user.useWebAuth = value === "useWebAuth";
    const urlParams = new URLSearchParams(window.location.search);
    serviceName = urlParams.get("name");

    const modus = urlParams.get("modus");
    const registerModus = Cookies.get("REGISTER_MODUS");
    if ((modus && modus === "cr") || registerModus) {
      $user.createAccount = true;
      domainNames().then(json => institutionDomainNames = json);
    }

    const testWebAuthn = urlParams.get("testWebAuthn");
    const email = urlParams.get("email")
    if (testWebAuthn && email) {
      $user.email = decodeURIComponent(email);
      $user.useWebAuth = true;
      webAuthnStart(email, true);
    }
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
    return $user.createAccount ? validEmail(email) && familyName && givenName && agreedWithTerms :
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
      if (institutionDomainNames.length === 0) {
        domainNames().then(json => {
          institutionDomainNames = json;
          handleEmailBlur({target: {value: $user.email}});
        });
      } else {
        handleEmailBlur({target: {value: $user.email}});
      }
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
      if (email) {
        const domain = email.substring(email.lastIndexOf("@") + 1);
        if (domain) {
          const domainLower = domain.toLowerCase();
          if (institutionDomainNames.some(name => name === domainLower || domainLower.endsWith("." + name))) {
            institutionDomainNameWarning = true;
            return;
          }
        }
      }
    }
    institutionDomainNameWarning = false;
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

<style>

    div.info-top {
        display: flex;
        flex-direction: column;
        margin-bottom: 30px;
    }

    div.info-top span {
        margin-left: auto;
    }

    /*div.info-top span:first-child {*/
    /*    margin-bottom: 5px;*/
    /*}*/
    a {
        text-decoration: underline;
    }

    a.toggle-link {
        color: #0062b0;
    }

    h2.header {
        font-size: 24px;
    }

    h2.top {
        margin: 0 0 15px 0;
        word-break: break-word;
        font-size: 16px;
    }

    h2.top span {
        color: var(--color-primary-green);
        font-size: 16px;
        font-family: Nunito, sans-serif;
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
        margin-bottom: 10px;
    }

    div.institution-warning {
        border-radius: 8px;
        background-color: #ffd89d;
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

    input[type=email], input[type=text], input[type=password] {
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

    div.password-option {
        margin-top: 10px;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<div class="info-top">
    {#if $user.createAccount}
        <span>{I18n.t("login.alreadyGuestAccount")} <a class="toggle-link" href="/login"
                                                       on:click|preventDefault|stopPropagation={createAccount(false)}>{I18n.t("login.loginEduId")}</a></span>
        <!--        <span><a class="toggle-link" target="_blank" href="https://eduid.nl">{I18n.t("login.whatis")}</a></span>-->
    {:else}
        <span>{I18n.t("login.requestEduId")} <a class="toggle-link" href="/reguest"
                                                on:click|preventDefault|stopPropagation={createAccount(true)}>{I18n.t("login.requestEduId2")}</a></span>
    {/if}
</div>

{#if $user.createAccount}
    <h2 class="header">{I18n.t("login.header2")}</h2>
    <h2 class="top">{I18n.t("login.headerSubTitle")}<span>{serviceName}</span></h2>
{:else}
    <h2 class="header">{I18n.t("login.header")}</h2>
    <h2 class="top">{I18n.t("login.headerSubTitle")}<span>{serviceName}</span></h2>
{/if}
{#if $user.createAccount}
    <label for="email" class="pre-input-label">{I18n.t("login.email")}</label>
{/if}
<input type="email"
       autocomplete="username"
       id="email"
       class={`${emailNotFound || emailOrPasswordIncorrect || emailInUse ? 'error' : ''}`}
       placeholder={I18n.t("login.emailPlaceholder")}
       use:init
       bind:value={$user.email}
       on:blur={handleEmailBlur}
       on:keydown={handleEmailEnter}>
{#if !$user.createAccount && emailNotFound}
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
{#if $user.createAccount && emailInUse}
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
{#if $user.createAccount && institutionDomainNameWarning}
    <div class="institution-warning">
        <span class="svg attention">{@html attention}</span>
        <div class="text">
            <span>{I18n.t("login.institutionDomainNameWarning")}</span>
            <br/>
            <span>{I18n.t("login.institutionDomainNameWarning2")}</span>
        </div>
    </div>

{/if}

{#if $user.createAccount}
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
{:else}
    <div id="password" class:hidden={!$user.usePassword || $user.useWebAuth}>
        <input type="password"
               autocomplete="current-password"
               id="password-field"
               placeholder={I18n.t("login.passwordPlaceholder")}
               on:keydown={handlePasswordEnter}
               bind:value={$user.password}
               bind:this={passwordField}>
    </div>

    <CheckBox value={$user.rememberMe}
              label={I18n.t("login.rememberMe")}
              onChange={val => $user.rememberMe = val}/>

    <div class="options">
        {#if $user.usePassword && !$user.useWebAuth}
            <Button href={`/${$user.usePassword ?  I18n.t("login.login") : I18n.t("login.usePassword")}`}
                    disabled={showSpinner ||!allowedNext($user.email, $user.familyName, $user.givenName, $user.password, true) && $user.usePassword}
                    label={$user.usePassword ?  I18n.t("login.login") : I18n.t("login.usePassword")}
                    className="full"
                    onClick={handleNext(true)}/>
            <LoginOptions/>
            <div class="password-option">
                <a href="/password"
                   on:click|preventDefault|stopPropagation={handleNext(false)}>{I18n.t("login.useMagicLink")}</a>
                <span>{I18n.t("login.useOr")}</span>
                <a href="/webauthn"
                   on:click|preventDefault|stopPropagation={switchWebAuthnPassword(true, false)}>{I18n.t("login.useWebAuth").toLowerCase()}</a>
            </div>
        {:else if !$user.usePassword && !$user.useWebAuth}
            <Button href="/magic"
                    disabled={showSpinner ||!allowedNext($user.email, $user.familyName, $user.givenName, $user.password, true) && !$user.usePassword}
                    label={$user.usePassword ?  I18n.t("login.useMagicLink") : I18n.t("login.sendMagicLink")}
                    className="full"
                    onClick={handleNext(false)}/>
            <LoginOptions/>
            <div class="password-option">
                <a href="/webauthn"
                   on:click|preventDefault|stopPropagation={switchWebAuthnPassword(true, false)}>{I18n.t("login.useWebAuth")}</a>
                <span>{I18n.t("login.useOr")}</span>
                <a href="/password"
                   on:click|preventDefault|stopPropagation={switchWebAuthnPassword(false, true)}>{I18n.t("login.usePassword")}</a>
            </div>
        {:else}
            <Button href="/webauthn"
                    disabled={showSpinner || (!validEmail($user.email) && !$user.usePassword)}
                    label={I18n.t("login.loginWebAuthn")}
                    className="full"
                    onClick={() => webAuthnStart($user.email)}/>
            <LoginOptions/>
            <div class="password-option">
                <a href="/magiclink"
                   on:click|preventDefault|stopPropagation={switchWebAuthnPassword(false, false)}>{I18n.t("login.useMagicLink")}</a>
                <span>{I18n.t("login.useOr")}</span>
                <a href="/password"
                   on:click|preventDefault|stopPropagation={switchWebAuthnPassword(false, true)}>{I18n.t("login.usePassword")}</a>
            </div>

        {/if}

    </div>
{/if}
