<script>
    import {config, flash, user} from "../stores/user";
    import I18n from "i18n-js";
    import verifiedSvg from "../icons/redesign/shield-full.svg";
    import Button from "../components/Button.svelte";
    import {startLinkAccountFlow, updateEmail, updateUser} from "../api";
    import Modal from "../components/Modal.svelte";
    import EditField from "../components/EditField.svelte";
    import {validEmail} from "../validation/regexp";
    import check from "../icons/redesign/check.svg";
    import {onMount} from "svelte";
    import {isEmpty} from "../utils/utils";

    let nameVerified = false;
    let studentVerified = false;
    let eduIDLinked = false;

    let nameVerifiedAccount = {};
    let studentVerifiedAccount = {};
    let eduIDLinkedAccount = {};

    let showNameDetails = false;
    let showStudentDetails = false;
    let showEduIDDetails = false;

    let outstandingPasswordForgotten = false;

    let tempEmailValue;
    let emailError = false;
    let emailErrorMessage = "";
    let emailEditMode = false;
    let callNameEditMode = false;
    let givenNameEditMode = false;
    let familyNameEditMode = false;

    let showModal = false;

    const addInstitution = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true
        } else {
            startLinkAccountFlow().then(json => {
                window.location.href = json.url;
            });
        }
    }

    const hasExpired = account => {
        const createdAt = new Date(account.createdAt);
        if (isEmpty(account.givenName) || isEmpty(account.familyName)) {
            createdAt.setDate(createdAt.getDate() + parseInt($config.expirationNonValidatedDurationDays, 10));
            return new Date() > createdAt;
        } else {
            return new Date() > new Date(account.expiresAt);
        }
    }

    const refresh = () => {
        showNameDetails = false;
        showStudentDetails = false;
        showEduIDDetails = false;
        const sortedAccounts = ($user.linkedAccounts || []).sort((a, b) => b.createdAt - a.createdAt);
        //Student verified and eduIDLinked expiry after
        const expiredLinkedAccounts = sortedAccounts.filter(account => !hasExpired(account));
        studentVerifiedAccount = expiredLinkedAccounts.find(account => (account.eduPersonAffiliations || [])
            .some(aff => aff && aff.startsWith("student"))) || {};
        studentVerified = studentVerifiedAccount !== undefined && Object.keys(studentVerifiedAccount).length > 0;

        eduIDLinked = expiredLinkedAccounts.length > 0;
        eduIDLinkedAccount = eduIDLinked ? expiredLinkedAccounts[0] : {};
        nameVerifiedAccount = sortedAccounts.find(account => account.givenName && account.familyName) || {};
        nameVerified = nameVerifiedAccount !== undefined && Object.keys(nameVerifiedAccount).length > 0;
    }

    const updateCallName = callName => {
        $user.callName = callName;
        callNameEditMode = false;
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
        if ($user.callName && $user.familyName && $user.givenName) {
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

    onMount(() => refresh());

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

        @media (max-width: 820px) {
            flex-direction: column;
            margin-top: 20px;
        }

        span.verified-badge {
            margin-left: 5px;
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
            margin: 0 auto 0 auto;
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
</style>
<div class="profile">
    <div class="inner-container">
        <h2>{I18n.t("profile.title")}</h2>
        <p class="info">{I18n.t("profile.info")}</p>
    </div>

    {#if !eduIDLinked}
        <div class="banner">
            <span class="verified-badge">{@html verifiedSvg}</span>
            <p>{I18n.t("profile.banner")}</p>
            <Button label={I18n.t("profile.verifyNow")}
                    className="ghost"
                    onClick={addInstitution(true)}/>
        </div>
    {/if}
    <div class="inner-container second">
        <div class="verified-container">
            <p class="info2">{I18n.t("profile.basic")}</p>
            {#if !eduIDLinked}
                <span class="not-verified">{I18n.t("profile.notVerified")}</span>
            {:else}
                <span class="verified">{@html check} {I18n.t("profile.verified")}</span>
            {/if}
        </div>
        <EditField label={I18n.t("profile.callName")}
                   firstValue={$user.callName || $user.givenName}
                   editableByUser={true}
                   saveLabel={I18n.t("edit.save")}
                   editMode={callNameEditMode}
                   onEdit={() => callNameEditMode = true}
                   onSave={value => updateCallName(value)}
                   onCancel={() => callNameEditMode = false}
        />
        <EditField label={I18n.t("profile.givenName")}
                   firstValue={$user.givenName}
                   editableByUser={!eduIDLinked}
                   saveLabel={I18n.t("edit.save")}
                   editMode={givenNameEditMode}
                   onEdit={() => givenNameEditMode = true}
                   onSave={value => updateGivenName(value)}
                   onCancel={() => givenNameEditMode = false}
        />
        <EditField label={I18n.t("profile.familyName")}
                   firstValue={$user.familyName}
                   editableByUser={!eduIDLinked}
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
                   saveLabel={I18n.t("email.update")}
                   errorMessage={emailErrorMessage}
                   editMode={emailEditMode}
                   onEdit={() => emailEditMode = true}
                   onSave={value => updateEmailValue(value)}
                   onCancel={() => cancelEmailEditMode()}
        />
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
    <Modal submit={addInstitution(false)}
           cancel={() => showModal = false}
           question={I18n.t(`profile.verifyFirstAndLastName.addInstitutionConfirmation`)}
           title={I18n.t(`profile.verifyFirstAndLastName.addInstitution`)}
           confirmTitle={I18n.t("profile.proceed")}>
    </Modal>
{/if}
