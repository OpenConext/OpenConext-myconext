<script>
    import {config, flash, user} from "../stores/user";
    import I18n from "../locale/I18n";
    import verifiedSvg from "../icons/redesign/shield-full.svg?raw";
    import personalInfo from "../icons/verify/personalInfo.svg?raw";
    import arrowLeft from "../icons/verify/arrow-left.svg?raw";
    import alertSvg from "../icons/alert-circle.svg?raw";
    import Button from "../components/Button.svelte";
    import {
        deleteLinkedAccount,
        generateEmailChangeCode,
        iDINIssuers,
        logout,
        me,
        preferLinkedAccount,
        resendMailChangeCode,
        startLinkAccountFlow,
        startVerifyAccountFlow,
        updateUser,
        verifyEmailChangeCode
    } from "../api";
    import Modal from "../components/Modal.svelte";
    import EditField from "../components/EditField.svelte";
    import {validEmail} from "../validation/regexp";
    import check from "../icons/redesign/check.svg?raw";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import {doLogOutAfterRateLimit, isEmpty} from "../utils/utils";
    import InstitutionRole from "../components/InstitutionRole.svelte";
    import {institutionName} from "../utils/services";
    import ValidatedData from "../components/ValidatedData.svelte";
    import VerifyChoice from "../verify/VerifyChoice.svelte";
    import {dateFromEpoch} from "../utils/date";
    import LinkedAccountSummary from "../components/LinkedAccountSummary.svelte";
    import CodeValidation from "../components/CodeValidation.svelte";

    const resendMailAllowedTimeOut = $config.emailSpamThresholdSeconds * 1000;

    let eduIDLinked = false;

    let sortedAccounts = [];
    let sortedExternalAccounts = [];
    let preferredAccount = null;
    let preferredInstitution = null;

    let outstandingPasswordForgotten = false;

    let tempEmailValue;
    let newEmailValue;
    let emailError = false;
    let emailErrorMessage = "";
    let emailEditMode = false;
    let chosenNameEditMode = false;
    let givenNameEditMode = false;
    let familyNameEditMode = false;
    let dayOfBirthEditMode = false;

    let showManageVerifiedInformation = false;
    let showModal = false;
    let serviceDeskStart = false;
    let showIdinOptions = false;
    let showDeleteInstitutionModal = false;
    let showNewInstitutionModal = false;
    let showPreferredInstitutionModal = false;
    let selectedInstitution;
    let newInstitution = {};
    let issuers;

    let showControlCode = false;

    let hasCodeValidation = false;
    let showCodeValidation = false;
    let wrongCode = false;
    let allowedToResend = false;
    let mailHasBeenResend = false;

    const manageVerifiedInformation = path => {
        navigate(`/${path}`, {replace: true});
    }

    const preferInstitution = (showConfirmation, linkedAccount) => {
        preferredInstitution = linkedAccount;
        if (showConfirmation) {
            showPreferredInstitutionModal = true;
        } else {
            preferLinkedAccount(preferredInstitution).then(res => {
                copyServerInformation(res);
                resetModalsAndQueryParams();
                flash.setValue(I18n.t("profile.preferred", {name: institutionName(linkedAccount)}));
            });
        }
    }

    const verifyCode = code => {
        verifyEmailChangeCode(code)
            .then(res => {
                navigate(`update-email?h=${res.hash}`)
            })
            .catch(e => {
                if (e.status === 403 || e.status === 400) {
                    doLogOutAfterRateLimit($config.idpBaseUrl);
                } else {
                    wrongCode = true;
                }
            })
    }

    const addInstitution = () => {
        startLinkAccountFlow().then(json => {
            window.location.href = json.url;
        });
    }
    const addBank = bankId => {
        startVerifyAccountFlow("idin", bankId).then(json => {
            window.location.href = json.url;
        });
    }
    const addEuropean = () => {
        startVerifyAccountFlow("eherkenning").then(json => {
            window.location.href = json.url;
        });
    }

    const addIdentity = showIdin => {
        showIdinOptions = showIdin;
        showModal = true;
    }

    const deleteInstitution = (showConfirmation, linkedAccount) => {
        selectedInstitution = linkedAccount;
        if (showConfirmation) {
            showDeleteInstitutionModal = true;
        } else {
            deleteLinkedAccount(linkedAccount).then(res => {
                showDeleteInstitutionModal = false;
                copyServerInformation(res);
                flash.setValue(I18n.t("Institution.Deleted.COPY", {name: institutionName(linkedAccount)}));
            });
        }
    }

    const markExternalLinkedAccountExpired = externalLinkedAccount => {
        externalLinkedAccount.expired = new Date() > new Date(externalLinkedAccount.expiresAt);
    }

    const markExpired = linkedAccount => {
        if (isEmpty(linkedAccount.givenName) || isEmpty(linkedAccount.familyName)) {
            const expiresAt = new Date(linkedAccount.createdAt);
            expiresAt.setDate(expiresAt.getDate() + parseInt($config.expirationNonValidatedDurationDays, 10));
            linkedAccount.expiresAt = expiresAt.getTime();
            linkedAccount.expired = new Date() > expiresAt;
        } else {
            linkedAccount.expired = new Date() > new Date(linkedAccount.expiresAt);
        }
    }

    const refresh = (retry = false) => {
        ($user.linkedAccounts || []).forEach(account => markExpired(account));
        ($user.externalLinkedAccounts || []).forEach(account => markExternalLinkedAccountExpired(account));
        sortedAccounts = ($user.linkedAccounts || []).sort((a, b) => b.createdAt - a.createdAt);
        sortedExternalAccounts = ($user.externalLinkedAccounts || []).sort((a, b) => b.createdAt - a.createdAt);
        const validLinkedAccounts = sortedAccounts.filter(account => !account.expired);
        const validExternalLinkedAccounts = sortedExternalAccounts.filter(account => !account.expired);
        const preferredLinkedAccount = validLinkedAccounts.find(account => account.preferred && !isEmpty(account.givenName) && !isEmpty(account.familyName));
        const preferredExternalLinkedAccount = validExternalLinkedAccounts.find(account => account.preferred);
        preferredAccount = preferredLinkedAccount || preferredExternalLinkedAccount;
        eduIDLinked = validLinkedAccounts.length > 0 || validExternalLinkedAccounts.length > 0;
        if (isEmpty($user.linkedAccounts) && isEmpty($user.externalLinkedAccounts) && !retry) {
            manageVerifiedInformation("personal");
        }
    }

    const updateChosenName = chosenName => {
        $user.chosenName = chosenName;
        chosenNameEditMode = false;
        doUpdateName();
    }

    const updateGivenName = givenName => {
        $user.givenName = givenName;
        givenNameEditMode = false;
        doUpdateName();
    }

    const updateFamilyName = familyName => {
        $user.familyName = familyName;
        familyNameEditMode = false;
        doUpdateName();
    }

    const doUpdateName = () => {
        if ($user.chosenName && $user.familyName && $user.givenName) {
            updateUser($user).then(() => {
                flash.setValue(I18n.t("Edit.Updated.COPY"));
            });
        }
    };

    const updateEmailValue = (value, force = false) => {
        if (validEmail(value) && value.toLowerCase() !== $user.email.toLowerCase()) {
            if (hasCodeValidation) {
                showCodeValidation = true;
            } else {
                generateEmailChangeCode(value, force).then(() => {
                    hasCodeValidation = true;
                    showCodeValidation = true;
                    flash.setValue(I18n.t("Email.UpdatedVerified.COPY", {email: value}), 6500);
                    tempEmailValue = null;
                    newEmailValue = value;
                    outstandingPasswordForgotten = false;
                    emailError = false;
                    emailErrorMessage = null;
                    emailEditMode = false;
                    setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
                }).catch(e => {
                    newEmailValue = null;
                    if (e.status === 409) {
                        emailError = true;
                        emailErrorMessage = I18n.t("Email.DuplicateEmail.COPY");
                    } else if (e.status === 406) {
                        tempEmailValue = value;
                        outstandingPasswordForgotten = true;
                    } else {
                        doLogOutAfterRateLimit($config.idpBaseUrl);
                    }
                });
            }
        }
    }

    const resendMail = () => {
        resendMailChangeCode()
            .then(() => {
                allowedToResend = false;
                setTimeout(() => allowedToResend = true, resendMailAllowedTimeOut);
            }).catch(() => {
            logout().then(() => navigate("/landing?ratelimit=true"));
        })
    }

    const valueCallback = values => {
        wrongCode = false;
    }

    const cancelEmailEditMode = () => {
        emailError = false;
        emailErrorMessage = false;
        emailEditMode = false;
    }

    const resetModalsAndQueryParams = () => {
        showDeleteInstitutionModal = false;
        showIdinOptions = false;
        showManageVerifiedInformation = false;
        showModal = false;
        showNewInstitutionModal = false;
        showPreferredInstitutionModal = false;
        showControlCode = false;
        const url = new URL(window.location.href);
        url.search = "";
        history.pushState({}, "", url.toString());
    }

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const retry = urlSearchParams.get("retry");
        const verify = urlSearchParams.get("verify");
        const linkedAccountIdentifier = urlSearchParams.get("institution");
        const showServiceDeskStart = urlSearchParams.get("servicedesk");

        showManageVerifiedInformation = window.location.pathname.indexOf("manage") > -1;

        refresh(!isEmpty(retry));

        const newAccountLinked = !isEmpty($user.linkedAccounts) && !isEmpty(linkedAccountIdentifier);
        const newExternalAccountLinked = !isEmpty(verify) && !isEmpty($user.externalLinkedAccounts);

        if (newAccountLinked || newExternalAccountLinked) {
            //Determine if the new account is external or not
            const newAccount = newExternalAccountLinked ?
                $user.externalLinkedAccounts[0] :
                ($user.linkedAccounts || [])
                    .find(la => la.eduPersonPrincipalName === linkedAccountIdentifier || la.subjectId === linkedAccountIdentifier);

            if (newAccount && (newExternalAccountLinked || !isEmpty(newAccount.givenName) || !isEmpty(newAccount.familyName))) {
                newInstitution = newAccount;
                if ((($user.linkedAccounts || []).length + ($user.externalLinkedAccounts || []).length) === 1) {
                    showNewInstitutionModal = true;
                } else {
                    preferInstitution(true, newAccount);
                }
            }
        }
        if (!isEmpty(retry)) {
            addIdentity(true);
            window.history.replaceState({}, document.title, "/personal");
        }
        if ($config.featureIdVerify && isEmpty(issuers)) {
            iDINIssuers().then(res => issuers = res);
        }
        if (!isEmpty(showServiceDeskStart)) {
            serviceDeskStart = true;
            showIdinOptions = true;
            showModal = true;
        }
    });

    const copyServerInformation = res => {
        for (let key in res) {
            if (res.hasOwnProperty(key)) {
                $user[key] = res[key];
            }
        }
        $user.controlCode = res.controlCode;
        refresh();
    }

    const refreshControlCode = () => {
        me().then(res => {
            copyServerInformation(res);
            showControlCode = !isEmpty(res.controlCode);
        });
    }

    const cancelVerifyChoice = () => {
        me().then(res => {
            copyServerInformation(res);
            resetModalsAndQueryParams();
        });

    }

</script>

<style lang="scss">
    $max-width-mobile: 1080px;
    $max-width-not-edit: 480px;

    .profile {
        width: 100%;
        height: 100%;
        @media (max-width: $max-width-mobile) {
            padding: 0 5px;
        }
    }

    .inner-container {
        padding: 35px 80px 15px 50px;
        display: flex;
        flex-direction: column;
        margin: 0 auto;

        @media (max-width: $max-width-mobile) {
            padding: 20px 0 15px 15px;
        }

        @media (max-width: 820px) {
            padding: 15px 0 10px 0;
        }

        &.second {
            padding: 0 10px 18px 50px;

            @media (max-width: $max-width-mobile) {
                padding: 0 20px 20px 20px;
            }
        }
    }

    .with-icon {
        display: flex;
        align-items: center;
        margin-bottom: 10px;

        span.back {
            margin-right: 25px;
            cursor: pointer;
        }
    }

    .verified-information {
        max-width: $max-width-not-edit;

        p.info {
            margin: 25px 0;
        }

        div.preferred-info {
            display: flex;


            :global(svg) {
                color: var(--color-primary-green);
                width: 20px;
                height: auto;
                margin: 0 10px auto 0;
            }
        }

        div.verified-account {
            border-top: 1px solid var(--color-primary-grey);
            margin-top: 25px;
            padding-top: 25px;

            &:last-child {
                margin-bottom: 40px;
            }
        }
    }


    .linked-accounts, .add-institution {
        max-width: $max-width-not-edit;
    }

    h2 {
        color: var(--color-primary-green);
    }

    p.info {
        font-weight: 300;
    }

    p.info-section {
        font-size: 22px;
        font-family: Nunito, sans-serif;
        color: var(--color-primary-green);

        &.second {
            margin: 25px 0 15px 0;
        }
    }

    p {
        line-height: 1.33;
        letter-spacing: normal;
    }

    div.banner {
        display: flex;
        align-items: center;
        background-color: var(--color-secondary-blue);
        padding: 15px 10px 15px 42px;

        @media (max-width: $max-width-mobile) {
            flex-direction: column;
            gap: 15px;
            align-items: flex-start;
            margin-top: 20px;
            padding: 10px;
        }

        :global(a.button.ghost) {
            max-width: 90px;
            margin: 0 0 0 auto;

            @media (max-width: $max-width-mobile) {
                margin-left: 0;
            }
        }

        &.expired {
            background-color: #fef8d3;
        }

        span.verified-badge {
            margin-left: 5px;
            @media (max-width: $max-width-mobile) {
                margin-left: 0;
            }

            :global(svg) {
                height: 28px;
                width: auto;
            }
        }

        p.banner-info {
            margin: 0 20px;
            @media (max-width: $max-width-mobile) {
                margin: 0;
            }
        }
    }

    .verified-container {
        display: flex;
        margin: 20px 0;
        max-width: $max-width-not-edit;
        align-items: center;


        span {
            margin-left: auto;
            padding: 6px;
            display: inline-flex;
            align-items: center;
            gap: 0.5em;
            border-radius: 8px;

            &.verified {
                background-color: var(--color-primary-green);
                color: white;
                border: 0.0625rem solid var(--color-primary-green);
            }

            &.not-verified {
                background-color: #efeeee;
                color: var(--color-secondary-grey);
                border: 0.0625rem solid var(--color-primary-grey);
            }
        }
    }

    .add-institution {
        padding: 15px;
        border: 2px solid var(--color-primary-grey);
        border-radius: 8px;
        display: flex;
        align-items: center;
        cursor: pointer;
        margin-bottom: 20px;

        &:hover {
            background-color: var(--color-background);
        }

        p {
            font-weight: 600;
            margin-bottom: 10px;
        }

        em {
            font-size: 14.5px;
        }

        span.add {
            font-size: 78px;
            margin-left: auto;
            color: var(--color-tertiare-grey);
        }

    }

    .control-code {
        display: flex;
        flex-direction: column;
        padding: 25px;

        span {
            margin: auto;

            &.code {
                font-size: 34px;
                margin-bottom: 40px;
                letter-spacing: 6px;
            }
        }
    }

    div.login-code {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    p.validation-info {
        text-align: center;
        margin-bottom: 40px;
    }

    h2.header {
        margin: 6px 0 30px 0;
        color: var(--color-primary-green);
        font-size: 28px;

        &.error {
            color: var(--color-primary-red);
        }
    }

    div.code-validation {
        margin-bottom: 40px;
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    p.error {
        margin-top: 10px;
        color: var(--color-primary-red);
    }

    div.resend-mail {
        margin-top: 30px;
        font-size: 15px;
        text-align: center;
    }

</style>
<div class="profile">
    {#if showManageVerifiedInformation}
        <div class="inner-container">
            <div class="verified-information">
                <div class="with-icon">
                <span class="back" on:click={() => manageVerifiedInformation("personal")}>
                    {@html arrowLeft}
                </span>
                    <h2>{I18n.t("YourVerifiedInformation.Title.COPY")}</h2>
                </div>
                <p class="info">{I18n.t("YourVerifiedInformation.Subtitle.COPY")}</p>
                <div class="preferred-info">
                    {@html personalInfo}
                    <p>{I18n.t("profile.defaultPreferred")}</p>
                </div>
                {#each sortedExternalAccounts as linkedAccount}
                    <div class="verified-account">
                        <LinkedAccountSummary deleteLinkedAccount={() => deleteInstitution(true, linkedAccount)}
                                              linkedAccount={linkedAccount}
                                              preferredAccount={linkedAccount.preferred}/>
                    </div>
                {/each}
                {#each sortedAccounts as linkedAccount}
                    <div class="verified-account">
                        <LinkedAccountSummary deleteLinkedAccount={() => deleteInstitution(true, linkedAccount)}
                                              linkedAccount={linkedAccount}
                                              preferredAccount={linkedAccount.preferred}/>
                    </div>
                {/each}
            </div>
        </div>

    {:else}
        <div class="inner-container">
            <div class="with-icon">
                <h2>{I18n.t("Profile.Title.COPY")}</h2>
            </div>
            <p class="info">{I18n.t("Profile.Info.COPY")}</p>
        </div>
        {#if $config.enableAccountLinking && !eduIDLinked}
            {#if isEmpty($user.linkedAccounts) && isEmpty($user.externalLinkedAccounts) && isEmpty($user.controlCode)}
                <div class="banner">
                    <span class="verified-badge">{@html verifiedSvg}</span>
                    <p class="banner-info">{I18n.t("profile.banner")}</p>
                    <Button label={I18n.t("Profile.VerifyNow.Button.COPY")}
                            className="ghost transparent"
                            onClick={() => addIdentity(true)}/>
                </div>
            {/if}
            {#if !isEmpty($user.linkedAccounts) || !isEmpty($user.externalLinkedAccounts)}
                <div class="banner expired">
                    <span class="verified-badge">{@html alertSvg}</span>
                    <p class="banner-info">{I18n.t("profile.expiredBanner")}</p>
                    <Button label={I18n.t("Profile.VerifyNow.Button.COPY")}
                            className="ghost transparent"
                            onClick={() => addIdentity(true)}/>
                </div>
            {/if}
            {#if isEmpty($user.linkedAccounts) && isEmpty($user.externalLinkedAccounts) && !isEmpty($user.controlCode)}
                <div class="banner expired">
                    <span class="verified-badge">{@html alertSvg}</span>
                    <p class="banner-info">{I18n.t("ServiceDesk.ControlCode.Banner.COPY")}</p>
                    <Button label={I18n.t("ServiceDesk.ControlCode.ShowCode.COPY")}
                            className="ghost transparent"
                            onClick={() => refreshControlCode()}/>
                </div>
            {/if}
        {/if}
        <div class="inner-container second">
            <div class="verified-container">
                <p class="info-section">{I18n.t("Profile.YourIdentity.COPY")}</p>
                {#if eduIDLinked}
                    <span class="verified">{@html check} {I18n.t("Profile.Verified.COPY")}</span>
                {:else}
                    <span class="not-verified">{I18n.t("Profile.NotVerified.COPY")}</span>
                {/if}
            </div>
            <EditField firstValue={$user.chosenName}
                       editableByUser={true}
                       editLabel={I18n.t("Login.GivenName.COPY")}
                       saveLabel={I18n.t("Email.Save.COPY")}
                       editMode={chosenNameEditMode}
                       onEdit={() => chosenNameEditMode = true}
                       onSave={value => updateChosenName(value)}
                       onCancel={() => chosenNameEditMode = false}
            />
            <EditField firstValue={$user.givenName}
                       editableByUser={!preferredAccount}
                       editLabel={I18n.t(`profile.${preferredAccount ? "validatedGivenName":"givenName"}`)}
                       manageVerifiedInformation={() => manageVerifiedInformation("manage")}
                       linkedAccount={preferredAccount}
                       saveLabel={I18n.t("Email.Save.COPY")}
                       editMode={givenNameEditMode}
                       onEdit={() => givenNameEditMode = true}
                       onSave={value => updateGivenName(value)}
                       onCancel={() => givenNameEditMode = false}
            />
            <EditField firstValue={$user.familyName}
                       editableByUser={!preferredAccount}
                       editLabel={I18n.t(`profile.${preferredAccount ? "validatedFamilyName":"familyName"}`)}
                       manageVerifiedInformation={() => manageVerifiedInformation("manage")}
                       linkedAccount={preferredAccount}
                       saveLabel={I18n.t("Email.Save.COPY")}
                       editMode={familyNameEditMode}
                       onEdit={() => familyNameEditMode = true}
                       onSave={value => updateFamilyName(value)}
                       onCancel={() => familyNameEditMode = false}
            />
            {#if !isEmpty($user.externalLinkedAccounts) && !isEmpty($user.dateOfBirth)}
                <EditField firstValue={dateFromEpoch($user.dateOfBirth)}
                           editableByUser={false}
                           editLabel={I18n.t(`Profile.VerifiedDateOfBirth.COPY`)}
                           manageVerifiedInformation={() => manageVerifiedInformation("manage")}
                           linkedAccount={$user.externalLinkedAccounts[0]}
                           saveLabel={I18n.t("Email.Save.COPY")}
                           editMode={dayOfBirthEditMode}
                           onEdit={() => dayOfBirthEditMode = true}
                           onSave={value => value}
                           onCancel={() => dayOfBirthEditMode = false}
                />
            {/if}
            <p class="info-section second">{I18n.t("Profile.ContactDetails.COPY")}</p>
            <EditField firstValue={$user.email}
                       editableByUser={true}
                       nameField={true}
                       error={emailError}
                       editLabel={I18n.t("Profile.Email.COPY")}
                       editHint={I18n.t("email.info")}
                       saveLabel={I18n.t("Email.Update.COPY")}
                       errorMessage={emailErrorMessage}
                       editMode={emailEditMode}
                       onEdit={() => emailEditMode = true}
                       onSave={value => updateEmailValue(value)}
                       onCancel={() => cancelEmailEditMode()}
            />
            {#if $config.enableAccountLinking}
                <p class="info-section second">{I18n.t("Profile.OrganisationsHeader.COPY")}</p>
                <section class="linked-accounts">
                    {#each sortedExternalAccounts.filter(acc => acc.idpScoping === "studielink" && !isEmpty(acc.eduPersonAffiliations)) as externalAccount}
                        <InstitutionRole manageVerifiedInformation={() => manageVerifiedInformation("manage")}
                                         linkedAccount={externalAccount}/>
                    {/each}
                    {#each sortedAccounts as account}
                        <InstitutionRole manageVerifiedInformation={() => manageVerifiedInformation("manage")}
                                         linkedAccount={account}/>
                    {/each}
                </section>
                <div class="add-institution"
                     on:click={() => addIdentity(false)}>
                    <div class="info">
                        <p>{I18n.t("Profile.AddAnOrganisation.COPY")}</p>
                        <em class="info">{I18n.t(`profile.${($config.featureIdVerify && isEmpty($user.externalLinkedAccounts)) ? "proceedVerify" : "proceedConext"}`)}</em>
                    </div>
                    <span class="add">+</span>
                </div>
            {/if}
        </div>
    {/if}
</div>
{#if outstandingPasswordForgotten}
    <Modal submit={() => updateEmailValue(tempEmailValue, true)}
           cancel={() => history.back()}
           warning={true}
           question={I18n.t("Email.OutstandingPasswordForgottenConfirmation.COPY")}
           title={I18n.t("Email.OutstandingPasswordForgotten.COPY")}>
    </Modal>
{/if}

{#if showModal || showControlCode}
    <Modal fixedWidth="400px"
            close={() => resetModalsAndQueryParams()}
           title={showIdinOptions ? I18n.t("WelcomeToApp.VerifyYour.Highlight.COPY") : showControlCode ?
    I18n.t("ServiceDesk.ControlCode.ControlCode.COPY") : I18n.t("Profile.AddAnOrganisation.COPY")}
           showOptions={false}>
        <VerifyChoice addInstitution={addInstitution}
                      addBank={addBank}
                      addEuropean={addEuropean}
                      issuers={issuers}
                      showIdinOptions={showIdinOptions}
                      showServiceDesk={serviceDeskStart}
                      showControlCode={showControlCode}
                      cancel={() => cancelVerifyChoice()}/>
    </Modal>
{/if}

{#if showDeleteInstitutionModal}
    <Modal submit={() => deleteInstitution(false, selectedInstitution)}
           cancel={() => showDeleteInstitutionModal = false}
           warning={true}
           confirmTitle={I18n.t("YourVerifiedInformation.ConfirmRemoval.Button.YesDelete.COPY")}
           question={I18n.t("Institution.DeleteInstitutionConfirmation.COPY")}
           title={I18n.t("YourVerifiedInformation.ConfirmRemoval.Title.COPY")}>
    </Modal>
{/if}

{#if showPreferredInstitutionModal}
    <Modal submit={() => preferInstitution(false, preferredInstitution)}
           cancel={() => resetModalsAndQueryParams()}
           confirmTitle={I18n.t("profile.yes")}
           cancelTitle={I18n.t("profile.no")}
           title={I18n.t("WelcomeToApp.VerifyYour.Highlight.COPY")}>
        <ValidatedData institution={newInstitution}
                       replacement={true}/>
    </Modal>
{/if}

{#if showNewInstitutionModal}
    <Modal submit={() => resetModalsAndQueryParams()}
           confirmTitle={I18n.t("NameUpdated.Continue.COPY")}
           largeConfirmation={true}
           title={I18n.t("WelcomeToApp.VerifyYour.Highlight.COPY")}>
        <ValidatedData institution={newInstitution}
                       readOnly={true}
        />
    </Modal>
{/if}

{#if showCodeValidation}
    <Modal showOptions={false}
           cancel={() => showCodeValidation = false}
           title={I18n.t("LoginCode.Title.COPY")}>
        <div class="login-code">
            <h2 class="header">{I18n.t("LoginCode.Header.COPY")}</h2>
            <p class="validation-info">{@html I18n.t("LoginCode.Info.COPY", {email: newEmailValue})}</p>
            <div class="code-validation">
                <CodeValidation verify={verifyCode}
                                size={6}
                                validate={val => !isNaN(val)}
                                intermediateCallback={valueCallback}/>
                {#if wrongCode}
                    <p class="error">{I18n.t("LoginCode.Error.COPY")}</p>
                {/if}
            </div>

            <div class="resend-mail">
                {#if allowedToResend}
                    <p>{I18n.t("LoginCode.Resend.COPY")}
                        <a href="resend"
                           on:click|preventDefault|stopPropagation={resendMail}>{I18n.t("LoginCode.ResendLink.COPY")}</a>
                    </p>
                {:else if mailHasBeenResend}
                    <span>{I18n.t("MagicLink.MailResend.COPY")}</span>
                {/if}

            </div>
        </div>

    </Modal>
{/if}
