<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import writeSvg from "../icons/redesign/pencil-write.svg";
    import verifiedSvg from "../icons/redesign/badgeclass.svg";
    import nonVerifiedSvg from "../icons/redesign/lock-shield.svg";
    import {onMount} from "svelte";
    import Button from "../components/Button.svelte";
    import {startLinkAccountFlow} from "../api";
    import Modal from "../components/Modal.svelte";
    import chevronDownSvg from "../icons/chevron-down.svg";

    let nameVerified = false;
    let studentVerified = false;
    let eduIDLinked = false;
    let verifiedName = undefined;
    let showModal = false;
    let showNameDetails = false;
    let showStudentDetails = false;
    let showNameDetails = false;

    onMount(() => {
        nameVerified = $user.linkedAccounts.length > 0;
        studentVerified = $user.linkedAccounts.length > 0 &&
            $user.linkedAccounts.find(account => (account.eduPersonAffiliations || []).some(aff => aff === "student"));
        eduIDLinked = $user.linkedAccounts.length > 0;
        if (nameVerified) {
            let lastLinkedAccount = $user.linkedAccounts.sort((a, b) => b.createdAt = a.createdAt)[0];
            verifiedName = `${lastLinkedAccount.givenName} ${lastLinkedAccount.familyName}`
        }
    });

    const addInstitution = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true
        } else {
            startLinkAccountFlow().then(json => {
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

  tr.name {
    cursor: pointer;
  }

  td {
    border-bottom: 1px solid var(--color-primary-grey);
  }

  td.attr {
    width: 30%;
    padding: 20px;
  }

  td.value {
    width: 70%;
    font-weight: bold;
    padding-left: 20px;

    &.long {
      font-weight: normal;
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
      max-width: 240px;
    }
  }

  div.value-container a.menu-link {
    margin-left: auto;
  }

  :global(div.value-container a.button) {
    margin-left: auto;
  }

  :global(a.menu-link svg) {
    color: var(--color-secondary-grey);
    width: 24px;
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
                        <a class="menu-link" href="/mail"
                           on:click|preventDefault|stopPropagation={() => navigate("/edit-mail")}>{@html writeSvg}</a>
                    </div>
                </td>
            </tr>
            <tr class="name" on:click={() => navigate("/edit")}>
                <td class="attr">{I18n.t("profile.name")}</td>
                <td class="verified"></td>
                <td class="value">
                    <div class="value-container">
                        <span>{`${$user.givenName} ${$user.familyName}`}</span>
                        <a class="menu-link" href="/name"
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
            <tr>
                <td class="attr">{I18n.t("profile.firstAndLastName")}</td>
                <td class="verified">{@html nameVerified ? verifiedSvg : nonVerifiedSvg}</td>
                <td class="value long">
                    <div class="value-container">
                        <span class:info={!nameVerified}>{I18n.t("profile.firstAndLastNameInfo")}</span>
                        {#if nameVerified}
                        {:else}
                            <Button href="/verify" label={I18n.t("profile.verify")} onClick={addInstitution(true)}
                                    small={true}/>
                        {/if}

                    </div>
                </td>
            </tr>
            <tr class="name" on:click={() => navigate("/edit")}>
                <td class="attr">{I18n.t("profile.name")}</td>
                <td></td>
                <td class="value">
                    <div class="value-container">
                        <span>{`${$user.givenName} ${$user.familyName}`}</span>
                        <a class="menu-link" href="/name"
                           on:click|preventDefault|stopPropagation={() => navigate("/edit-name")}>{@html writeSvg}</a>
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