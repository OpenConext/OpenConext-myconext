<script>
    import {config, user} from "../stores/user";
    import {link, navigate} from "svelte-routing";
    import {validEmail} from "../validation/regexp";
    import I18n from "../locale/I18n";
    import critical from "../icons/critical.svg?raw";
    import attention from "../icons/alert-circle.svg?raw";

    import CheckBox from "../components/CheckBox.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import Button from "../components/Button.svelte";
    import {domains} from "../stores/domains";
    import {
        allowedEmailDomains,
        createInstitutionEduID,
        fetchInstitutionEduID,
        institutionalEmailDomains
    } from "../api";

    export let hash;

    let userInfo = {};
    let emailInUse = false;
    let emailForbidden = false;
    let initial = true;
    let showSpinner = true;

    let agreedWithTerms = false;

    const updateEmail = e => {
        const email = e.target.value.replace(/[<>]/g, "");
        $user.email = e.target.value = email;
        $domains.allowedDomainNamesError = false;
        $domains.institutionDomainNameWarning = false;
        emailForbidden = false;
        emailInUse = false;
    }

    onMount(() => {
        fetchInstitutionEduID(hash).then(res => {
            userInfo = res;
            showSpinner = false;
        })
        $user.email = "";
        if ($config.featureWarningEducationalEmailDomain) {
            institutionalEmailDomains().then(json => {
                $domains.institutionDomainNames = json;
            });
        }
        if ($config.featureAllowList) {
            allowedEmailDomains().then(json => {
                $domains.allowedDomainNames = json;
            })
        }
    });

    const handleNext = (newUser = true) => {
        if (allowedNext($user.email, agreedWithTerms)) {
            showSpinner = true;
            createInstitutionEduID($user.email, hash, newUser)
                .then(res => {
                    const url = `/create-from-institution/poll/${hash}`;
                    navigate(url, {replace: true})
                })
                .catch(e => {
                    showSpinner = false;
                    if (e.status === 409) {
                        emailInUse = true;
                    } else if (e.status === 412) {
                        emailForbidden = true;
                    } else {
                        navigate("/expired", {replace: true});
                    }
                });
        } else {
            initial = false;
        }
    };

    const init = el => el.focus();

    const allowedNext = (email, agreedWithTerms) => {
        return validEmail(email) && agreedWithTerms && !$domains.allowedDomainNamesError
    };

    const handleEmailBlur = e => {
        const email = e.target.value;
        //defensive, in edge-cases we don't validate
        if (email && email.lastIndexOf("@") > -1) {
            const domain = email.substring(email.lastIndexOf("@") + 1);
            if (domain) {
                const domainLower = domain.toLowerCase();
                if ($config.featureAllowList && !$domains.allowedDomainNames.some(name => name === domainLower || domainLower.endsWith("." + name))) {
                    $domains.allowedDomainNamesError = true;
                    return;
                }
                if ($config.featureWarningEducationalEmailDomain && $domains.institutionDomainNames.some(name => name === domainLower || domainLower.endsWith("." + name))) {
                    $domains.institutionDomainNameWarning = true;
                    return;
                }
            }
        }
        $domains.institutionDomainNameWarning = false;
        $domains.allowedDomainNamesError = false;
    }

</script>

<style lang="scss">

    .link-from-institution {
        display: flex;
        flex-direction: column;
        background-color: white;
        height: auto;
        min-height: 500px;
        align-items: center;
        align-content: center;
    }

    div.inner {
        margin: 25px auto auto 200px;
        max-width: 600px;

        @media (max-width: 800px) {
            margin: 25px auto;
        }
    }

    h3 {
        color: var(--color-primary-green);
        margin-bottom: 40px;
    }

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
        margin: 5px 0 15px 0;
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

    input[type=email] {
        border: 1px solid #727272;
        border-radius: 6px;
        padding: 14px;
        font-size: 16px;
        width: 100%;
        margin: 15px 0 5px 0;
    }

    input.error {
        border: solid 1px var(--color-primary-red);
        background-color: #fff5f3;
    }

    div.actions {
        margin-top: 25px;
    }

    input.error:focus {
        outline: none;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<div class="link-from-institution">

    <div class="inner">

        <h3 class="header">{I18n.t("LinkFromInstitution.Header.COPY", {name: `${userInfo.given_name}`})}</h3>
        <p>{I18n.t("LinkFromInstitution.Info.COPY")}</p>
        <input type="email"
               autocomplete="username"
               id="email"
               spellcheck="false"
               class:error={emailInUse || emailForbidden}
               placeholder={I18n.t("LinkFromInstitution.EmailPlaceholder.COPY")}
               use:init
               on:input={updateEmail}
               on:blur={handleEmailBlur}>
        {#if !initial && !validEmail($user.email)}
            <div class="error">
                <span class="svg">{@html critical}</span>
                <span>{I18n.t("LinkFromInstitution.InvalidEmail.COPY")}</span>
            </div>
        {/if}
        {#if emailInUse}
            <div class="error">
                <span class="svg">{@html critical}</span>
                <div>
                    <span>{I18n.t("LinkFromInstitution.EmailInUse1.COPY")}</span>
                    <span>{I18n.t("LinkFromInstitution.EmailInUse2.COPY")}</span>
                    <a href="/next"
                       on:click|preventDefault|stopPropagation={() => handleNext(false)}>
                        {I18n.t("LinkFromInstitution.EmailInUse3.COPY")}
                    </a>
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
            <span>{I18n.t("LinkFromInstitution.AllowedDomainNamesError.COPY",
                {domain: $user.email.substring($user.email.indexOf("@") + 1)})}</span>
                    <br/>
                    <span>{I18n.t("LinkFromInstitution.AllowedDomainNamesError2.COPY")}</span>
                </div>
            </div>

        {/if}

        <CheckBox value={agreedWithTerms}
                  className="light"
                  terms={true}
                  label={I18n.t("LinkFromInstitution.AgreeWithTerms.COPY")}
                  onChange={val => agreedWithTerms = val}/>
        <div class="actions">
            <Button disabled={showSpinner || !allowedNext($user.email, agreedWithTerms)}
                    href="/finish"
                    large={true}
                    label={I18n.t("LinkFromInstitution.RequestEduIdButton.COPY")}
                    onClick={handleNext}/>

        </div>
    </div>
</div>