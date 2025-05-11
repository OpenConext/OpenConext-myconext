<script>
    import {user} from "../stores/user";
    import {conf, links} from "../stores/conf";
    import {link, navigate} from "svelte-routing";
    import {validEmail} from "../constants/regexp";
    import I18n from "../locale/I18n";
    import critical from "../icons/critical.svg?raw";
    import attention from "../icons/alert-circle.svg?raw";

    import {fetchServiceName, generateCodeNewUser} from "../api/index";
    import CheckBox from "../components/CheckBox.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import Button from "../components/Button.svelte";
    import {domains} from "../stores/domains";
    import Cookies from "js-cookie";
    import {cookieNames} from "../constants/cookieNames";

    export let id;
    let emailInUse = false;
    let emailForbidden = false;
    let initial = true;
    let showSpinner = true;
    let serviceName = "";

    let agreedWithTerms = false;

    onMount(() => {
        $links.displayBackArrow = true;
        $user.givenName = "";
        $user.familyName = "";
        $user.knowUser = null;
        Cookies.remove(cookieNames.USERNAME);
        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const handleNext = () => {
        if (allowedNext($user.email, $user.givenName, $user.familyName, agreedWithTerms)) {
            showSpinner = true;
            generateCodeNewUser($user.email, $user.givenName, $user.familyName, id)
                .then(res => {
                    const url = res.stepup ? `/stepup/${id}?name=${encodeURIComponent(serviceName)}&explanation=${res.explanation}` :
                        `/code/${id}?name=${encodeURIComponent(serviceName)}&modus=cr`;
                    navigate(url, {replace: true})
                })
                .catch(e => {
                    showSpinner = false;
                    if (e.status === 409) {
                        emailInUse = true;
                        emailForbidden = false;
                    } else if (e.status === 412) {
                        emailForbidden = true;
                        emailInUse = false;
                    } else {
                        navigate("/expired", {replace: true});
                    }
                });
        } else {
            initial = false;
        }
    };

    const init = el => el.focus();

    const handleInput = e => {
        const email = (e.target.value || "").replace(/[<>]/g, "");
        e.target.value = email;
        $user.email = email;
    }

    const allowedNext = (email, familyName, givenName, agreedWithTerms) => {
        return validEmail(email) && familyName && givenName && agreedWithTerms && !$domains.allowedDomainNamesError
    };

    const handleEmailBlur = e => {
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
        $domains.institutionDomainNameWarning = false;
        $domains.allowedDomainNamesError = false;
    }

    const updateGivenName = e => {
        const givenName = e.target.value.replace(/[<>]/g, "");
        $user.givenName = e.target.value = givenName;
    }

    const handleGivenNameBlur = e => {
        const givenName = e.target.value.trim();
        $user.givenName = e.target.value = givenName;
    }

    const updateFamilyName = e => {
        const familyName = e.target.value.replace(/[<>]/g, "");
        $user.familyName = e.target.value = familyName;
    }

    const handleFamilyNameBlur = e => {
        const familyName = e.target.value.trim();
        $user.familyName = e.target.value = familyName;
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

    .pre-input-label {
        color: var(--color-primary-black);
        font-weight: 600;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}

<h2 class="header">{I18n.t("LinkFromInstitution.RequestEduIdButton.COPY")}</h2>
{#if serviceName}
    <h2 class="top">{I18n.t("Login.HeaderSubTitle.COPY")}<span>{serviceName}</span></h2>
{/if}
<label for="email" class="pre-input-label">{I18n.t("LinkFromInstitution.Email.COPY")}</label>
<input type="email"
       autocomplete="username"
       id="email"
       spellcheck="false"
       class:error={emailInUse || emailForbidden}
       placeholder={I18n.t("LinkFromInstitution.EmailPlaceholder.COPY")}
       use:init
       on:input={handleInput}
       value={$user.email}
       on:blur={handleEmailBlur}>
{#if !initial && !validEmail($user.email)}
    <div class="error"><span class="svg">{@html critical}</span><span>{I18n.t("LinkFromInstitution.InvalidEmail.COPY")}</span></div>
{/if}
{#if emailInUse}
    <div class="error">
        <span class="svg">{@html critical}</span>
        <div>
            <span>{I18n.t("LinkFromInstitution.EmailInUse1.COPY")}</span>
            <span>{I18n.t("LinkFromInstitution.EmailInUse2.COPY")}</span>
            <a use:link
               href={`/login/${id}`}
            >{I18n.t("Login.EmailInUse3.COPY")}</a>
        </div>
    </div>
{/if}
{#if emailForbidden}
    <div class="error">
        <span class="svg">{@html critical}</span>
        <div>
            <span>{@html I18n.t("LinkFromInstitution.EmailForbidden.COPY")}</span>
        </div>
    </div>
{/if}
{#if $domains.institutionDomainNameWarning}
    <div class="institution-warning">
        <span class="svg attention">{@html attention}</span>
        <div class="text">
            <span>{I18n.t("LinkFromInstitution.InstitutionDomainNameWarning.COPY")}</span>
            <br/>
            <span>{I18n.t("LinkFromInstitution.InstitutionDomainNameWarning2.COPY")}</span>
        </div>
    </div>

{/if}

{#if $domains.allowedDomainNamesError}
    <div class="domain-not-allowed">
        <span class="svg error">{@html critical}</span>
        <div class="text">
            <span>{I18n.t("Login.AllowedDomainNamesError.COPY",
                {domain: $user.email.substring($user.email.indexOf("@") + 1)})}</span>
            <br/>
            <span>{I18n.t("LinkFromInstitution.AllowedDomainNamesError2.COPY")}</span>
        </div>
    </div>

{/if}

<label for="given-name" class="pre-input-label">{I18n.t("Profile.FirstName.COPY")}</label>
<input type="text"
       id="given-name"
       placeholder={I18n.t("Login.GivenNamePlaceholder.COPY")}
       spellcheck="false"
       on:input={updateGivenName}
       on:blur={handleGivenNameBlur}>
{#if !initial && !$user.givenName}
    <span class="error">{I18n.t("Login.RequiredAttribute.COPY", {attr: I18n.t("Profile.FirstName.COPY")})}</span>
{/if}
<label for="family-name" class="pre-input-label">{I18n.t("Profile.LastName.COPY")}</label>
<input type="text"
       id="family-name"
       spellcheck="false"
       placeholder={I18n.t("Login.FamilyNamePlaceholder.COPY")}
       on:input={updateFamilyName}
       on:blur={handleFamilyNameBlur}>
{#if !initial && !$user.familyName}
    <span class="error">{I18n.t("Login.RequiredAttribute.COPY", {attr: I18n.t("Profile.LastName.COPY")})}</span>
{/if}
<CheckBox value={agreedWithTerms}
          className="light"
          label={I18n.t("LinkFromInstitution.AgreeWithTerms.COPY")}
          onChange={val => agreedWithTerms = val}/>
<Button disabled={showSpinner || !allowedNext($user.email, $user.familyName, $user.givenName, agreedWithTerms)}
        href="/magic"
        className="full"
        label={I18n.t("LinkFromInstitution.RequestEduIdButton.COPY")}
        onClick={handleNext}/>
