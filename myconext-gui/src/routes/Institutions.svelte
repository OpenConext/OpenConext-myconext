<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import chevron_right from "../icons/chevron-right.svg";
    import Modal from "../components/Modal.svelte";
    import Button from "../components/Button.svelte";
    import {startLinkAccountFlow} from "../api";

    let showModal = false;

    const institutionDetails = linkedAccount => () =>
            navigate(`/institution?name=${encodeURIComponent(linkedAccount.schacHomeOrganization)}`);

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

<style>
    .institutions {
        width: 100%;
        height: 100%;
    }

    .inner {
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
        margin-bottom: 26px;
    }

    p {
        font-size: 18px;
        line-height: 1.33;
        letter-spacing: normal;
    }

    p.no-institutions {
        margin-top: 25px;
        font-style: italic;
    }
    table {
        margin-top: 30px;
        width: 100%;
    }

    tr {
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
    }

    div.value-container {
        display: flex;
        align-items: center;
    }

    div.value-container a.menu-link {
        margin-left: auto;
    }

    div.options {
        display: flex;
        margin-top: 65px;
    }

    :global(a.menu-link svg) {
        fill: var(--color-primary-green);
    }


</style>
<div class="institutions">
    <div class="inner">
        <h2>{I18n.t("institutions.title")}</h2>
        <p class="info">{I18n.t("institutions.info")}</p>
        <p class="info">{I18n.t("institutions.explanation")}</p>
        {#if $user.linkedAccounts.length === 0}
            <p class="no-institutions">{I18n.t("institutions.noInstitutions")}</p>
        {:else}
            <table cellspacing="0">
                <thead></thead>
                <tbody>
                {#each $user.linkedAccounts as linkedAccount}
                    <tr class="name" on:click={institutionDetails(linkedAccount)}>
                        <td class="attr">{I18n.t("institutions.name")}</td>
                        <td class="value">
                            <div class="value-container">
                                <span>{`${linkedAccount.schacHomeOrganization}`}</span>
                                <a class="menu-link" href="/edit"
                                   on:click|preventDefault|stopPropagation={institutionDetails(linkedAccount)}>
                                    {@html chevron_right}
                                </a>
                            </div>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        {/if}
        <div class="options">
            <Button label={I18n.t("institutions.add")} onClick={addInstitution(true)}/>
        </div>

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
