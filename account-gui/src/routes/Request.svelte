<script>
    import {user} from "../stores/user";
    import {conf, links} from "../stores/conf";
    import {navigate} from "svelte-routing";
    import {validEmail} from "../constants/regexp";
    import I18n from "../locale/I18n";
    import critical from "../icons/critical.svg?raw";
    import {FriendlyCaptchaSDK} from "@friendlycaptcha/sdk"
    import {fetchServiceName, generateCodeNewUser} from "../api/index";
    import CheckBox from "../components/CheckBox.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import Button from "../components/Button.svelte";
    import {domains} from "../stores/domains";
    import Cookies from "js-cookie";
    import {cookieNames} from "../constants/cookieNames";
    import {isEmpty} from "../utils/utils.js";
    import Alert from "../components/Alert.svelte";
    import AlertType from "../constants/AlertType.js";
    import SubContent from "../components/SubContent.svelte";

    export let id;

    let emailInUse = false;
    let emailForbidden = false;
    let initial = true;
    let showSpinner = true;
    let serviceName = "";
    let agreedWithTerms = false;
    let captchaShowWarning = false;

    onMount(() => {
        $links.displayBackArrow = true;
        $user.givenName = "";
        $user.familyName = "";
        $user.knowUser = null;
        Cookies.remove(cookieNames.USERNAME);
        if ($conf.captchaEnabled) {
            const mount = document.querySelector("#captcha");
            mount.addEventListener("frc:widget.complete", () => captchaShowWarning = false);

            const sdk = new FriendlyCaptchaSDK();
            const language = I18n.currentLocale();
            sdk.createWidget({
                element: mount,
                sitekey: $conf.captchaSiteKey,
                startMode: "auto",
                language: language,
            });
        }

        fetchServiceName(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const handleNext = () => {
        const captchaResponse = document.querySelector("[name='frc-captcha-response']")?.value;
        if ($conf.captchaEnabled && isEmpty(captchaResponse) || captchaResponse === ".ACTIVATED") {
            captchaShowWarning = true;
            initial = false;
            return;
        }
        if (allowedNext($user.email, $user.givenName, $user.familyName, agreedWithTerms)) {
            showSpinner = true;
            generateCodeNewUser($user.email, $user.givenName, $user.familyName, id, captchaResponse)
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
        return validEmail(email) && familyName && givenName && agreedWithTerms && !$domains.allowedDomainNamesError && !captchaShowWarning
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

    const otherEmail = () => {
        $user.email = "";
        agreedWithTerms = false;
        emailInUse = false;
        emailForbidden = false;
        $domains.institutionDomainNameWarning = false;
        $domains.allowedDomainNamesError = false;
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
        display: block;
        margin: 0 auto 10px 0;
        color: var(--color-primary-red);
    }

    input[type=email], input[type=text] {
        border: 1px solid #B2B6BE;
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

    .controls {
        margin: 10px 0 10px 0;
        display: flex;
        flex-direction: column;
        gap: 10px
    }

    #captcha {
        width: 335px !important;
    }

    :global(.frc-banner) {
        display: none;
    }

    .or-divider{
      display: flex;
      justify-content: center;
      align-items: center;

      margin-top: 25px;
      .or-divider-text {
        margin-left: 10px;
        margin-right: 10px;
        color: #5E6873;
      }
      .stripe {
        margin-top: 3px;
        height: 1px;
        width: 100%;
        background: #000;
      }
    }

    .message-email-in-use{
      padding-top: 12px;
    }

    .header {
      margin-bottom: 15px;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<form on:keydown={(e) => e.key === 'Enter' && e.preventDefault()}>
    <h2 class="header">{I18n.t("Login.RequestHeader.COPY")}</h2>

    <h2 class="top">{I18n.t("Login.RequestSubHeader.COPY")}</h2>

    <label for="email" class="pre-input-label">{I18n.t("LinkFromInstitution.Email.COPY")}</label>
    <input type="email"
           autocomplete="username"
           id="email"
           spellcheck="false"
           class:error={emailForbidden}
           placeholder={I18n.t("LinkFromInstitution.EmailPlaceholder.COPY")}
           use:init
           on:input={handleInput}
           value={$user.email}
           on:blur={handleEmailBlur}>
    {#if !initial && !validEmail($user.email)}
        <div class="error"><span
                class="svg">{@html critical}</span><span>{I18n.t("LinkFromInstitution.InvalidEmail.COPY")}</span></div>
    {/if}
    {#if emailInUse}

        <div class="message-email-in-use">
            <Alert
                    message={I18n.t("Login.EmailInUse1.COPY")}
                    alertType={AlertType.Warning}
                    class="message-email-in-use"
            />
        </div>
        <Button href="/login/${id}"
                label={I18n.t("Login.LoginWithThisEmail.COPY")}
                useLink="true"/>

        <div class="or-divider">
            <div class="stripe"></div>
            <span class="or-divider-text">{I18n.t("Login.OrDivider.COPY")}</span>
            <div class="stripe"></div>
        </div>

        <Button href="/"
                label={I18n.t("Login.TryOtherEmail.COPY")}
                onClick={otherEmail}
                className="secondary"
        />

    {/if}
    {#if emailForbidden}
        <div class="error">
            <span class="svg">{@html critical}</span>
            <div>
                <span>{@html I18n.t("LinkFromInstitution.EmailForbidden.COPY")}</span>
            </div>
        </div>
    {/if}
    {#if $domains.institutionDomainNameWarning && !emailInUse}
        <Alert
                message={I18n.t("LinkFromInstitution.InstitutionDomainNameWarning.COPY")}
                alertType={AlertType.Warning}
        />
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
    <div hidden={emailInUse}>
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
        <div class="controls">
            <CheckBox value={agreedWithTerms}
                      className="light"
                      label={I18n.t("LinkFromInstitution.AgreeWithTerms.COPY")}
                      onChange={val => agreedWithTerms = val}/>
            {#if !initial && !agreedWithTerms}
                <span class="error">{I18n.t("login.termsRequired")}</span>
            {/if}
            <div id="captcha" class="frc-captcha"></div>
            {#if captchaShowWarning}
                <span class="captcha error">{I18n.t("captcha.proveNotRobot")}</span>
            {/if}
            <Button disabled={!initial && (showSpinner || !allowedNext($user.email, $user.familyName, $user.givenName, agreedWithTerms))}
                    href="/magic"
                    className="full"
                    label={I18n.t("LinkFromInstitution.RequestEduIdButton.COPY")}
                    onClick={handleNext}/>
        </div>
    </div>
</form>
<div hidden={emailInUse}>
    <SubContent question={I18n.t("Login.AlreadyGuestAccount.COPY")}
                linkText={I18n.t("Login.LoginEduId.COPY")}
                route="/login/{id}" interContent="true"/>
</div>