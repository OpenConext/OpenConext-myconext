<script>
    import {config, flash, user} from "../stores/user";
    import I18n from "i18n-js";
    import verifiedSvg from "../icons/redesign/shield-full.svg";
    import personalInfo from "../icons/verify/personalInfo.svg";
    import arrowLeft from "../icons/verify/arrow-left.svg";
    import alertSvg from "../icons/alert-circle.svg";
    import Button from "../components/Button.svelte";
    import {
        deleteLinkedAccount,
        iDINIssuers,
        preferLinkedAccount,
        startLinkAccountFlow,
        startVerifyAccountFlow,
        updateEmail,
        updateUser
    } from "../api";
    import Modal from "../components/Modal.svelte";
    import EditField from "../components/EditField.svelte";
    import {validEmail} from "../validation/regexp";
    import check from "../icons/redesign/check.svg";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import {isEmpty} from "../utils/utils";
    import InstitutionRole from "../components/InstitutionRole.svelte";
    import {institutionName} from "../utils/services";
    import ValidatedData from "../components/ValidatedData.svelte";
    import VerifyChoice from "../verify/VerifyChoice.svelte";
    import {dateFromEpoch} from "../utils/date";
    import LinkedAccountSummary from "../components/LinkedAccountSummary.svelte";

    let eduIDLinked = false;

    let sortedAccounts = [];
    let sortedExternalAccounts = [];
    let preferredAccount = null;
    let preferredInstitution = null;

    let outstandingPasswordForgotten = false;

    let tempEmailValue;
    let emailError = false;
    let emailErrorMessage = "";
    let emailEditMode = false;
    let chosenNameEditMode = false;
    let givenNameEditMode = false;
    let familyNameEditMode = false;
    let dayOfBirthEditMode = false;

    let showManageVerifiedInformation = false;
    let showModal = false;
    let showIdinOptions = false;
    let showDeleteInstitutionModal = false;
    let showNewInstitutionModal = false;
    let showPreferredInstitutionModal = false;
    let showPostVerificationModal = false;
    let selectedInstitution;
    let newInstitution = {};
    let issuers;

    const manageVerifiedInformation = path => {
        navigate(`/${path}`, {replace:true});
    }

    const preferInstitution = (showConfirmation, linkedAccount) => {
        preferredInstitution = linkedAccount;
        if (showConfirmation) {
            showPreferredInstitutionModal = true;
        } else {
            preferLinkedAccount(preferredInstitution).then(json => {
                for (let key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                        refresh();
                    }
                }
                resetModalsAndQueryParams();
                flash.setValue(I18n.t("profile.preferred", {name: institutionName(linkedAccount)}));
            });
        }
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
            deleteLinkedAccount(linkedAccount).then(json => {
                showDeleteInstitutionModal = false;
                for (let key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                        refresh();
                    }
                }
                flash.setValue(I18n.t("institution.deleted", {name: institutionName(linkedAccount)}));
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

    const refresh = (retry=false) => {
        ($user.linkedAccounts || []).forEach(account => markExpired(account));
        ($user.externalLinkedAccounts || []).forEach(account => markExternalLinkedAccountExpired(account));
        sortedAccounts = ($user.linkedAccounts || []).sort((a, b) => b.createdAt - a.createdAt);
        sortedExternalAccounts = ($user.externalLinkedAccounts || []).sort((a, b) => b.createdAt - a.createdAt);
        const validLinkedAccounts = sortedAccounts.filter(account => !account.expired);
        const validExternalLinkedAccount = !isEmpty($user.externalLinkedAccounts) && !$user.externalLinkedAccounts[0].expired;
        const linkedAccount = validLinkedAccounts.find(account => account.preferred) || validLinkedAccounts[0];
        if (isEmpty(linkedAccount) || isEmpty(linkedAccount.givenName) || isEmpty(linkedAccount.familyName)) {
            preferredAccount = null;
        } else {
            preferredAccount = linkedAccount;
            linkedAccount.preferred = true;
        }
        if (!isEmpty($user.externalLinkedAccounts)) {
            if (isEmpty(preferredAccount) || preferredAccount.createdAt < $user.externalLinkedAccounts[0].createdAt) {
                preferredAccount = $user.externalLinkedAccounts[0];
                $user.externalLinkedAccounts[0].preferred = true;
                validLinkedAccounts.forEach(acc => acc.preferred = false);
                preferredAccount.external = true;
            }

        }
        eduIDLinked = validLinkedAccounts.length > 0 || validExternalLinkedAccount;
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
                flash.setValue(I18n.t("edit.updated"));
            });
        }
    };

    const updateEmailValue = (value, force = false) => {
        if (validEmail(value) && value.toLowerCase() !== $user.email.toLowerCase()) {
            updateEmail({...$user, email: value}, force)
                .then(() => {
                    flash.setValue(I18n.t("email.updated", {email: value}), 6500);
                    tempEmailValue = null;
                    emailError = false;
                    emailErrorMessage = null;
                    emailEditMode = false;
                }).catch(e => {
                if (e.status === 409) {
                    emailError = true;
                    emailErrorMessage = I18n.t("email.duplicateEmail");
                } else if (e.status === 406) {
                    tempEmailValue = value;
                    outstandingPasswordForgotten = true;
                }
            });
        }
    };

    const cancelEmailEditMode = () => {
        emailError = false;
        emailErrorMessage = false;
        emailEditMode = false;
    }

    const resetModalsAndQueryParams = () => {
        showNewInstitutionModal = false;
        showManageVerifiedInformation = false;
        showModal = false;
        showIdinOptions = false;
        showDeleteInstitutionModal = false;
        showNewInstitutionModal = false;
        showPreferredInstitutionModal = false;
        showPostVerificationModal = false;
        const url = new URL(window.location.href);
        url.search = "";
        history.pushState({}, "", url.toString());
    }

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const retry = urlSearchParams.get("retry");
        const verify = urlSearchParams.get("verify");
        const eduPersonPrincipalName = urlSearchParams.get("institution");

        showManageVerifiedInformation = window.location.pathname.indexOf("manage") > -1;
        refresh(retry);
        if (!isEmpty($user.linkedAccounts)) {
            if (!isEmpty(eduPersonPrincipalName)) {
                const institution = $user.linkedAccounts.find(ins => ins.eduPersonPrincipalName === eduPersonPrincipalName);
                if (institution && !isEmpty(institution.givenName) && !isEmpty(institution.familyName)) {
                    newInstitution = institution;
                    if (($user.linkedAccounts || []).length === 1) {
                        showNewInstitutionModal = true;
                    } else {
                        preferInstitution(true, institution);
                    }
                }
            }
        }
        if (!isEmpty(verify) && !isEmpty($user.externalLinkedAccounts)) {
            showPostVerificationModal = true;
        }
        if (!isEmpty(retry)) {
            addIdentity(true);
            window.history.replaceState({}, document.title, "/personal");
        }
        if ($config.featureIdVerify && isEmpty(issuers)) {
            iDINIssuers().then(res => issuers = res);
        }
    });

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
        padding: 10px 10px 10px 42px;

        @media (max-width: $max-width-mobile) {
            flex-direction: column;
            gap: 15px;
            align-items: flex-start;
            margin-top: 20px;
            padding: 10px;
        }

        :global(a.button.ghost) {
            max-width: 90px;
            margin-left: auto;

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

</style>
<div class="profile">
    {#if showManageVerifiedInformation}
        <div class="inner-container">
            <div class="verified-information">
            <div class="with-icon">
                <span class="back" on:click={() => manageVerifiedInformation("personal")}>
                    {@html arrowLeft}
                </span>
                <h2>{I18n.t("profile.verifiedInformation")}</h2>
            </div>
                <p class="info">{I18n.t("profile.verifiedInformationInfo")}</p>
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
                <h2>{I18n.t("profile.title")}</h2>
            </div>
            <p class="info">{I18n.t("profile.info")}</p>
        </div>

        {#if !eduIDLinked && isEmpty($user.linkedAccounts) && isEmpty($user.externalLinkedAccounts)}
            <div class="banner">
                <span class="verified-badge">{@html verifiedSvg}</span>
                <p class="banner-info">{I18n.t("profile.banner")}</p>
                <Button label={I18n.t("profile.verifyNow")}
                        className="ghost transparent"
                        onClick={() => addIdentity(true)}/>
            </div>
        {/if}
        {#if !eduIDLinked && (!isEmpty($user.linkedAccounts) || !isEmpty($user.externalLinkedAccounts))}
            <div class="banner expired">
                <span class="verified-badge">{@html alertSvg}</span>
                <p class="banner-info">{I18n.t("profile.expiredBanner")}</p>
                <Button label={I18n.t("profile.verifyNow")}
                        className="ghost transparent"
                        onClick={() => addIdentity(true)}/>
            </div>
        {/if}
        <div class="inner-container second">
            <div class="verified-container">
                <p class="info-section">{I18n.t("profile.basic")}</p>
                {#if eduIDLinked}
                    <span class="verified">{@html check} {I18n.t("profile.verified")}</span>
                {:else}
                    <span class="not-verified">{I18n.t("profile.notVerified")}</span>
                {/if}
            </div>
            <EditField firstValue={$user.chosenName}
                       editableByUser={true}
                       editLabel={I18n.t("profile.chosenName")}
                       saveLabel={I18n.t("edit.save")}
                       editMode={chosenNameEditMode}
                       onEdit={() => chosenNameEditMode = true}
                       onSave={value => updateChosenName(value)}
                       onCancel={() => chosenNameEditMode = false}
            />
            <EditField firstValue={$user.givenName}
                       editableByUser={!preferredAccount && !$user.givenNameVerified}
                       editLabel={I18n.t(`profile.${preferredAccount && $user.givenNameVerified ? "validatedGivenName":"givenName"}`)}
                       manageVerifiedInformation={() => manageVerifiedInformation("manage")}
                       linkedAccount={preferredAccount}
                       saveLabel={I18n.t("edit.save")}
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
                       saveLabel={I18n.t("edit.save")}
                       editMode={familyNameEditMode}
                       onEdit={() => familyNameEditMode = true}
                       onSave={value => updateFamilyName(value)}
                       onCancel={() => familyNameEditMode = false}
            />
            {#if !isEmpty($user.externalLinkedAccounts) && !isEmpty($user.dateOfBirth)}
                <EditField firstValue={dateFromEpoch($user.dateOfBirth)}
                           editableByUser={false}
                           editLabel={I18n.t(`profile.validatedDayOfBirth`)}
                           manageVerifiedInformation={() => manageVerifiedInformation("manage")}
                           linkedAccount={$user.externalLinkedAccounts[0]}
                           saveLabel={I18n.t("edit.save")}
                           editMode={dayOfBirthEditMode}
                           onEdit={() => dayOfBirthEditMode = true}
                           onSave={value => value}
                           onCancel={() => dayOfBirthEditMode = false}
                />
            {/if}
            <p class="info-section second">{I18n.t("profile.contact")}</p>
            <EditField firstValue={$user.email}
                       editableByUser={true}
                       nameField={true}
                       error={emailError}
                       editLabel={I18n.t("profile.email")}
                       editHint={I18n.t("email.info")}
                       saveLabel={I18n.t("email.update")}
                       errorMessage={emailErrorMessage}
                       editMode={emailEditMode}
                       onEdit={() => emailEditMode = true}
                       onSave={value => updateEmailValue(value)}
                       onCancel={() => cancelEmailEditMode()}
            />
            <p class="info-section second">{I18n.t("profile.role")}</p>
            <section class="linked-accounts">
                {#each sortedAccounts as account}
                    <InstitutionRole manageVerifiedInformation={() => manageVerifiedInformation("manage")}
                                     linkedAccount={account}/>
                {/each}
            </section>
            <div class="add-institution"
                 on:click={() => addIdentity(false)}>
                <div class="info">
                    <p>{I18n.t("profile.addInstitution")}</p>
                    <em class="info">{I18n.t(`profile.${($config.featureIdVerify && isEmpty($user.externalLinkedAccounts)) ? "proceedVerify" : "proceedConext"}`)}</em>
                </div>
                <span class="add">+</span>
            </div>


        </div>

    {/if}
</div>
{#if outstandingPasswordForgotten}
    <Modal submit={() => updateEmailValue($user.email, true)}
           cancel={() => history.back()}
           warning={true}
           question={I18n.t("email.outstandingPasswordForgottenConfirmation")}
           title={I18n.t("email.outstandingPasswordForgotten")}>
    </Modal>
{/if}

{#if showModal}
    <Modal close={() => resetModalsAndQueryParams()}
           title={showIdinOptions ? I18n.t("verify.modal.header") : I18n.t("profile.addInstitution")}
           showOptions={false}>
        <VerifyChoice addInstitution={addInstitution}
                      addBank={addBank}
                      addEuropean={addEuropean}
                      issuers={issuers}
                      showIdinOptions={showIdinOptions}
                      cancel={() => resetModalsAndQueryParams()}/>
    </Modal>
{/if}

{#if showDeleteInstitutionModal}
    <Modal submit={() => deleteInstitution(false, selectedInstitution)}
           cancel={() => showDeleteInstitutionModal = false}
           warning={true}
           confirmTitle={I18n.t("modal.delete")}
           question={I18n.t("institution.deleteInstitutionConfirmation")}
           title={I18n.t("institution.deleteInstitution")}>
    </Modal>
{/if}

{#if showPreferredInstitutionModal}
    <Modal submit={() => preferInstitution(false, preferredInstitution)}
           cancel={() => resetModalsAndQueryParams()}
           confirmTitle={I18n.t("profile.yes")}
           cancelTitle={I18n.t("profile.no")}
           title={I18n.t("profile.preferInstitution")}>
        <ValidatedData institution={newInstitution}
                       replacement={true}/>
    </Modal>
{/if}

{#if showPostVerificationModal}
    <Modal submit={() => resetModalsAndQueryParams()}
           confirmTitle={I18n.t("profile.ok")}
           largeConfirmation={true}
           title={I18n.t("verify.modal.header")}>
        <ValidatedData institution={$user.externalLinkedAccounts[0]}/>
    </Modal>

{/if}

{#if showNewInstitutionModal}
    <Modal submit={() => resetModalsAndQueryParams()}
           confirmTitle={I18n.t("profile.ok")}
           largeConfirmation={true}
           title={I18n.t("verify.modal.header")}>
        <ValidatedData institution={newInstitution}
                       readOnly={true}
        />
    </Modal>
{/if}
