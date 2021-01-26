<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import writeSvg from "../icons/redesign/pencil-write.svg";
    import verifiedSvg from "../icons/redesign/shield-full.svg";
    import nonVerifiedSvg from "../icons/redesign/shield-empty.svg";
    import {onMount} from "svelte";
    import Button from "../components/Button.svelte";
    import {startLinkAccountFlow} from "../api";
    import Modal from "../components/Modal.svelte";
    import chevronDownSvg from "../icons/chevron-down.svg";
    import chevronUpSvg from "../icons/chevron-up.svg";
    import {formatCreateDate} from "../format/date";

    let nameVerified = false;
    let studentVerified = false;
    let eduIDLinked = false;
    let linkedAccount = undefined;
    let verifiedStudent = undefined;
    let showModal = false;
    let showNameDetails = false;
    let showStudentDetails = false;
    let showTrustedDetails = false;

    onMount(() => {
        const back = window.localStorage.getItem("back");
        if (back) {
            window.localStorage.removeItem("back");
            navigate(`/${back}`);
        }
        nameVerified = $user.linkedAccounts.length > 0;
        studentVerified = $user.linkedAccounts.length > 0 &&
            $user.linkedAccounts.find(account => (account.eduPersonAffiliations || []).some(aff => aff === "student"));
        eduIDLinked = $user.linkedAccounts.length > 0;
        if (nameVerified) {
            linkedAccount = $user.linkedAccounts.sort((a, b) => b.createdAt = a.createdAt)[0];
            verifiedStudent = $user.linkedAccounts.map(account => account.eduPersonAffiliations).flat().join(", ");
        }
    });

    const addInstitution = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true
        } else {
            startLinkAccountFlow("personal").then(json => {
                window.location.href = json.url;
            });
        }
    }


</script>

<style lang="scss">
    .profile {
        width: 100%;
        height: 100%;
    }

    .inner-container {
        height: 100%;
        margin: 0 auto;
        padding: 15px 30px 15px 0;
        display: flex;
        flex-direction: column;
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
        margin-top: 28px;
        margin-bottom: 24px;
        font-family: Nunito, sans-serif;
    }

    p {
        font-size: 18px;
        line-height: 1.33;
        letter-spacing: normal;
    }

    table {
        width: 100%;
    }

    tr.full {
        background-color: var(--color-background);
    }

    tr.name {
        cursor: pointer;
    }

    td {
        border-bottom: 1px solid var(--color-primary-grey);
    }

    td.attr {
        width: 30%;
        padding: 20px;
        font-weight: normal;
    }

    td.value {
        width: 70%;
        font-weight: bold;
        padding-left: 20px;

        &.details {
            padding-left: 0;
        }

        table {
            width: 100%;
            background-color: var(--color-background);

            tr:last-child {
                td {
                    border-bottom: none;
                }
            }

            td {
                padding: 10px;
            }
        }
    }

    :global(td.verified svg) {
        width: 22px;
        height: auto;
    }

    @media (max-width: 820px) {
        td.verified {
            display: none;
        }
    }

    div.value-container {
        display: flex;
        align-items: center;
        width: 100%;
    }

    div.value-container span {
        word-break: break-word;

        &.info {
            max-width: 270px;
            font-weight: normal;
        }
    }

    div.value-container a.right-link, div.value-container a.toggle-link {
        margin-left: auto;
    }

    :global(div.value-container a.button) {
        margin-left: auto;
    }

    :global(a.right-link svg) {
        color: var(--color-secondary-grey);
        width: 22px;
        height: auto;
    }

    :global(a.toggle-link svg) {
        fill: var(--color-secondary-grey);
        width: 30px;
        height: auto;
    }

</style>
<div class="profile">
    <div class="inner-container">
        <h2>{I18n.t("profile.title")}</h2>
        <p class="info">{I18n.t("profile.info")}</p>
        <p class="info2">{I18n.t("profile.basic")}</p>
        <table cellspacing="0">
            <thead></thead>
            <tbody>
            <tr>
                <td class="attr">{I18n.t("profile.email")}</td>
                <td class="verified">{@html verifiedSvg}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{$user.email}</span>
                        <a class="right-link" href="/mail"
                           on:click|preventDefault|stopPropagation={() => navigate("/edit-email")}>{@html writeSvg}</a>
                    </div>
                </td>
            </tr>
            <tr class="name" on:click={() => navigate("/edit")}>
                <td class="attr">{I18n.t("profile.name")}</td>
                <td class="verified"></td>
                <td class="value">
                    <div class="value-container">
                        <span>{`${$user.givenName} ${$user.familyName}`}</span>
                        <a class="right-link" href="/name"
                           on:click|preventDefault|stopPropagation={() => navigate("/edit-name")}>{@html writeSvg}</a>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
        <p class="info2">{I18n.t("profile.validated")}</p>
        <table cellspacing="0">
            <thead></thead>
            <tbody>
            <tr class:full={showNameDetails}>
                <td class="attr">{I18n.t("profile.firstAndLastName")}</td>
                <td class="verified">{@html nameVerified ? verifiedSvg : nonVerifiedSvg}</td>
                <td class="value">
                    <div class="value-container">
                        {#if nameVerified}
                            <span>{`${linkedAccount.givenName} ${linkedAccount.familyName}`}</span>
                            <a class="toggle-link" href="/"
                               on:click|preventDefault|stopPropagation={() => showNameDetails = !showNameDetails}>
                                {@html showNameDetails ? chevronUpSvg : chevronDownSvg}
                            </a>
                        {:else}
                            <span class="info">{I18n.t("profile.firstAndLastNameInfo")}</span>
                            <Button href="/verify" label={I18n.t("profile.verify")} onClick={addInstitution(true)}
                                    small={true}/>
                        {/if}

                    </div>
                </td>
            </tr>
            {#if showNameDetails}
                <tr class="full">
                    <td class="attr"></td>
                    <td class="verified"></td>
                    <td class="value details">
                        <table class="inner-details">
                            <thead></thead>
                            <tbody>
                            <tr>
                                <td class="attr">{I18n.t("profile.institution")}</td>
                                <td class="value">{linkedAccount.schacHomeOrganization}</td>
                            </tr>
                            <tr>
                                <td class="attr">{I18n.t("profile.affiliations")}</td>
                                <td class="value">{linkedAccount.eduPersonAffiliations.join(", ")}</td>
                            </tr>
                            <tr>
                                <td class="attr">{I18n.t("profile.expires")}</td>
                                <td class="value">
                                    {I18n.t("profile.expiresValue", formatCreateDate(linkedAccount.expiresAt))}
                                </td>
                            </tr>
                            </tbody>
                        </table>

                    </td>
                </tr>
            {/if}
            <tr>
                <td class="attr">{I18n.t("profile.student")}</td>
                <td class="verified">{@html studentVerified ? verifiedSvg : nonVerifiedSvg}</td>
                <td class="value">
                    <div class="value-container" class:full={showStudentDetails}>
                        {#if showStudentDetails}
                            <span>{verifiedStudent}</span>
                            <a class="toggle-link" href="/"
                               on:click|preventDefault|stopPropagation={() => showStudentDetails = !showStudentDetails}>
                                {@html chevronDownSvg}
                            </a>
                            {#if showStudentDetails}

                            {/if}
                        {:else}
                            <span class="info">{I18n.t("profile.studentInfo")}</span>
                            <Button href="/prove" label={I18n.t("profile.prove")} onClick={addInstitution(true)}
                                    small={true}/>
                        {/if}

                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

</div>
{#if showModal}
    <Modal submit={addInstitution(false)}
           cancel={() => showModal = false}
           question={I18n.t("institutions.addInstitutionConfirmation")}
           title={I18n.t("institutions.addInstitution")}
           confirmTitle={I18n.t("institutions.proceed")}>
    </Modal>
{/if}