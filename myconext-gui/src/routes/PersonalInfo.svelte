<script>
    import {user} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import writeSvg from "../icons/redesign/pencil-write.svg";
    import verifiedSvg from "../icons/redesign/shield-full.svg";
    import {onMount} from "svelte";
    import VerifiedUserRow from "../components/VerifiedUserRow.svelte";

    let nameVerified = false;
    let studentVerified = false;
    let eduIDLinked = false;

    let nameVerifiedAccount = {};
    let studentVerifiedAccount = {};
    let eduIDLinkedAccount = {};

    let showNameDetails = false;
    let showStudentDetails = false;
    let showEduIDDetails = false;

    const refresh = () => {
        showNameDetails = false;
        showStudentDetails = false;
        showEduIDDetails = false;

        const sortedAccounts = ($user.linkedAccounts || []).sort((a, b) => b.createdAt = a.createdAt);

        studentVerifiedAccount = sortedAccounts.find(account => (account.eduPersonAffiliations || []).some(aff => aff === "student")) || {};
        studentVerified = studentVerifiedAccount != undefined && Object.keys(studentVerifiedAccount).length > 0;

        eduIDLinked = $user.linkedAccounts.length > 0;
        eduIDLinkedAccount = eduIDLinked ? sortedAccounts[0] : {};

        nameVerifiedAccount = sortedAccounts.find(account => account.givenName && account.familyName) || {};
        nameVerified = nameVerifiedAccount !== undefined && Object.keys(nameVerifiedAccount).length > 0;

    }

    refresh();

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
        line-height: 1.33;
        letter-spacing: normal;
    }

    table {
        width: 100%;

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

            a.right-link {
                margin-left: auto;
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
            <VerifiedUserRow
                    bind:showDetails={showNameDetails}
                    attr={I18n.t("profile.firstAndLastName")}
                    verified={nameVerified}
                    account={nameVerifiedAccount}
                    verifiedValue={`${nameVerifiedAccount.givenName} ${nameVerifiedAccount.familyName}`}
                    info={I18n.t("profile.firstAndLastNameInfo")}
                    buttonTxt={I18n.t("profile.verify")}
                    refresh={refresh}/>
            <VerifiedUserRow
                    bind:showDetails={showStudentDetails}
                    attr={I18n.t("profile.student")}
                    verified={studentVerified}
                    account={studentVerifiedAccount}
                    verifiedValue={`${studentVerifiedAccount.schacHomeOrganization} ${(studentVerifiedAccount.eduPersonAffiliations || []).join(", ")}`}
                    info={I18n.t("profile.studentInfo")}
                    buttonTxt={I18n.t("profile.prove")}
                    refresh={refresh}/>
            <VerifiedUserRow
                    bind:showDetails={showEduIDDetails}
                    attr={I18n.t("profile.trusted")}
                    verified={eduIDLinked}
                    account={eduIDLinkedAccount}
                    verifiedValue={`${eduIDLinkedAccount.schacHomeOrganization}`}
                    info={I18n.t("profile.trustedInfo")}
                    buttonTxt={I18n.t("profile.link")}
                    refresh={refresh}/>
            </tbody>
        </table>
    </div>

</div>
