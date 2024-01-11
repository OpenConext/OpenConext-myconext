<script>
    import {config, flash, user} from "../stores/user";
    import I18n from "i18n-js";
    import verifiedSvg from "../icons/redesign/shield-full.svg";
    import alertSvg from "../icons/alert-circle.svg";
    import Button from "../components/Button.svelte";
    import {deleteLinkedAccount, preferLinkedAccount, startLinkAccountFlow, updateEmail, updateUser} from "../api";
    import Modal from "../components/Modal.svelte";
    import EditField from "../components/EditField.svelte";
    import {validEmail} from "../validation/regexp";
    import check from "../icons/redesign/check.svg";
    import {onMount} from "svelte";
    import {isEmpty} from "../utils/utils";
    import InstitutionRole from "../components/InstitutionRole.svelte";
    import {institutionName} from "../utils/services";
    import ValidatedData from "../components/ValidatedData.svelte";

    let eduIDLinked = false;

    let sortedAccounts = [];
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

    let showModal = false;
    let showDeleteInstitutionModal = false;
    let showNewInstitutionModal = false;
    let showPreferredInstitutionModal = false;
    let selectedInstitution;
    let newInstitution = {};

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
                showPreferredInstitutionModal = false;
                flash.setValue(I18n.t("profile.preferred", {name: institutionName(linkedAccount)}));
            });
        }
    }

    const addInstitution = showConfirmation => {
        if (showConfirmation) {
            showModal = true
        } else {
            startLinkAccountFlow().then(json => {
                window.location.href = json.url;
            });
        }
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

    const markExpired = account => {
        const expiredAt = new Date(account.createdAt);
        expiredAt.setDate(expiredAt.getDate() + parseInt($config.expirationNonValidatedDurationDays, 10));
        account.expiresAtRole = expiredAt;
        account.expiredRole = new Date() > account.expiresAtRole;
        if (isEmpty(account.givenName) || isEmpty(account.familyName)) {
            account.expired = account.expiredRole;
            account.expiresAtNonValidated = account.expiresAtRole.getTime();
        } else {
            account.expired = new Date() > new Date(account.expiresAt);
        }
    }

    const refresh = () => {
        ($user.linkedAccounts || []).forEach(account => markExpired(account));
        sortedAccounts = ($user.linkedAccounts || []).sort((a, b) => b.createdAt - a.createdAt);
        const validLinkedAccounts = sortedAccounts.filter(account => !account.expired);
        const linkedAccount = validLinkedAccounts.find(account => account.preferred) || validLinkedAccounts[0];
        if (isEmpty(linkedAccount) || isEmpty(linkedAccount.givenName) || isEmpty(linkedAccount.familyName)) {
            preferredAccount = null;
        } else {
            preferredAccount = linkedAccount;
        }
        eduIDLinked = validLinkedAccounts.length > 0;
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

    onMount(() => {
        refresh();
        if (($user.linkedAccounts || []).length > 0) {
            const urlSearchParams = new URLSearchParams(window.location.search);
            const eduPersonPrincipalName = urlSearchParams.get("institution");
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
    });

</script>

<style lang="scss">
    .profile {
        width: 100%;
        height: 100%;
        @media (max-width: 820px) {
            padding: 0 5px;
        }
    }

    .inner-container {
        padding: 15px 80px 15px 50px;
        display: flex;
        flex-direction: column;
        margin: 0 auto;

        @media (max-width: 820px) {
            padding: 0 0 0 0;
        }

        &.second {
            padding: 0 80px 15px 50px;

            @media (max-width: 820px) {
                padding: 0 0 0 0;
            }
        }
    }

    h2 {
        margin: 20px 0 10px 0;
        color: var(--color-primary-green);
    }

    p.info {
        font-weight: 300;
    }

    p.info2 {
        font-size: 22px;
        font-family: Nunito, sans-serif;
    }

    p {
        line-height: 1.33;
        letter-spacing: normal;
    }

    div.banner {
        display: flex;
        align-items: center;
        background-color: var(--color-secondary-blue);
        padding: 10px;

        :global(a.button.ghost) {
            max-width: 90px;
            margin-left: auto;
        }

        &.expired {
            background-color: #fef8d3;
        }

        @media (max-width: 820px) {
            flex-direction: column;
            margin-top: 20px;
        }

        span.verified-badge {
            margin-left: 5px;

            :global(svg) {
                height: 28px;
                width: auto;
            }
        }

        p {
            margin: 0 5px 0 15px;
        }
    }

    .verified-container {
        display: flex;
        margin-top: 28px;
        margin-bottom: 24px;
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

    p.label {
        margin-top: 40px;
        font-weight: 600;
        margin-bottom: 10px;
    }

    .add-institution {
        padding: 15px;
        border: 2px solid var(--color-primary-grey);
        border-radius: 8px;
        display: flex;
        align-items: center;
        cursor: pointer;
        margin-bottom: 20px;

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
    <div class="inner-container">
        <h2>{I18n.t("profile.title")}</h2>
        <p class="info">{I18n.t("profile.info")}</p>
    </div>

    {#if !eduIDLinked && $user.linkedAccounts.length === 0}
        <div class="banner">
            <span class="verified-badge">{@html verifiedSvg}</span>
            <p>{I18n.t("profile.banner")}</p>
            <Button label={I18n.t("profile.verifyNow")}
                    className="ghost transparent"
                    onClick={() => addInstitution(true)}/>
        </div>
    {/if}
    {#if !eduIDLinked && $user.linkedAccounts.length > 0}
        <div class="banner expired">
            <span class="verified-badge">{@html alertSvg}</span>
            <p>{I18n.t("profile.expiredBanner")}</p>
            <Button label={I18n.t("profile.verifyNow")}
                    className="ghost transparent"
                    onClick={() => addInstitution(true)}/>
        </div>
    {/if}
    <div class="inner-container second">
        <div class="verified-container">
            <p class="info2">{I18n.t("profile.basic")}</p>
            {#if eduIDLinked}
                <span class="verified">{@html check} {I18n.t("profile.verified")}</span>
            {:else}
                <span class="not-verified">{I18n.t("profile.notVerified")}</span>
            {/if}
        </div>
        <EditField label={I18n.t("profile.chosenName")}
                   firstValue={$user.chosenName}
                   editableByUser={true}
                   saveLabel={I18n.t("edit.save")}
                   editMode={chosenNameEditMode}
                   onEdit={() => chosenNameEditMode = true}
                   onSave={value => updateChosenName(value)}
                   onCancel={() => chosenNameEditMode = false}
        />
        <EditField label={I18n.t("profile.givenName")}
                   firstValue={$user.givenName}
                   editableByUser={!preferredAccount}
                   addInstitution={addInstitution}
                   linkedAccount={preferredAccount}
                   saveLabel={I18n.t("edit.save")}
                   editMode={givenNameEditMode}
                   onEdit={() => givenNameEditMode = true}
                   onSave={value => updateGivenName(value)}
                   onCancel={() => givenNameEditMode = false}
        />
        <EditField label={I18n.t("profile.familyName")}
                   firstValue={$user.familyName}
                   editableByUser={!preferredAccount}
                   addInstitution={addInstitution}
                   linkedAccount={preferredAccount}
                   saveLabel={I18n.t("edit.save")}
                   editMode={familyNameEditMode}
                   onEdit={() => familyNameEditMode = true}
                   onSave={value => updateFamilyName(value)}
                   onCancel={() => familyNameEditMode = false}
        />
        <EditField label={I18n.t("profile.email")}
                   firstValue={$user.email}
                   editableByUser={true}
                   nameField={false}
                   error={emailError}
                   editHint={I18n.t("email.info")}
                   saveLabel={I18n.t("email.update")}
                   errorMessage={emailErrorMessage}
                   editMode={emailEditMode}
                   onEdit={() => emailEditMode = true}
                   onSave={value => updateEmailValue(value)}
                   onCancel={() => cancelEmailEditMode()}
        />
        <p class="label">{I18n.t("profile.linkedAccounts")}</p>
        <section class="linked-accounts">
            {#each sortedAccounts as account}
                <InstitutionRole addInstitution={addInstitution}
                                 removeInstitution={deleteInstitution}
                                 linkedAccount={account}/>
            {/each}
        </section>
        <div class="add-institution" on:click={() => addInstitution(true)}>
            <div class="info">
                <p>{I18n.t("profile.addInstitution")}</p>
                <em class="info">{I18n.t("profile.proceedConext")}</em>
            </div>
            <span class="add">+</span>
        </div>


    </div>
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
    <Modal submit={() => addInstitution(false)}
           cancel={() => showModal = false}
           question={I18n.t(`profile.verifyFirstAndLastName.addInstitutionConfirmation`)}
           title={I18n.t(`profile.verifyFirstAndLastName.addInstitution`)}
           confirmTitle={I18n.t("profile.proceed")}>
    </Modal>
{/if}

{#if showDeleteInstitutionModal}
    <Modal submit={() => deleteInstitution(false, selectedInstitution)}
           cancel={() => showDeleteInstitutionModal = false}
           warning={true}
           question={I18n.t("institution.deleteInstitutionConfirmation")}
           title={I18n.t("institution.deleteInstitution")}>
    </Modal>
{/if}

{#if showPreferredInstitutionModal}
    <Modal submit={() => preferInstitution(false, preferredInstitution)}
           cancel={() => showPreferredInstitutionModal = false}
           question={I18n.t("profile.preferredInstitutionConfirmation", {name: institutionName(preferredInstitution)})}
           confirmTitle={I18n.t("profile.yes")}
           cancelTitle={I18n.t("profile.no")}
           title={I18n.t("profile.preferInstitution")}>
        <ValidatedData institution={newInstitution}/>
    </Modal>
{/if}

{#if showNewInstitutionModal}
    <Modal submit={() => showNewInstitutionModal = false}
           question={I18n.t("profile.newInstitutionInfo")}
           confirmTitle={I18n.t("profile.ok")}
           title={I18n.t("profile.newInstitution")}>
        <ValidatedData institution={newInstitution}/>
    </Modal>
{/if}
