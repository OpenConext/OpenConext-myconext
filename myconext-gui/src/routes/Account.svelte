<script>
    import {user} from "../stores/user";
    import {formatCreateDate} from "../format/date";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";

    import Button from "../components/Button.svelte";
    import Modal from "../components/Modal.svelte";

    let showModal = false;

    const addLeadingZero = s => s.length < 2 ? `${0}s` : s;

    const personalDataFileName = () => {
        const today = new Date();
        return `eduid_export_${addLeadingZero(today.getDate())}${addLeadingZero(today.getMonth() + 1)}${today.getFullYear()}.json`
    }

</script>

<style lang="scss">
    .account {
        width: 100%;
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

    table {
        width: 100%;

        td {
            border-bottom: 1px solid var(--color-primary-grey);
            padding: 15px 0 15px 0;

            &.attr {
                width: 35%;
            }

            &.value {
                width: 65%;
                font-weight: 600;
            }
        }
    }

</style>

<div class="account">

    <h2>{I18n.t("account.title")}</h2>
    <p class="info">{I18n.t("account.info")}</p>
    <table cellspacing="0">
        <thead/>
        <tbody>
        {#if $user.created && $user.created > 0}
            <tr>
                <td class="attr">{I18n.t("account.created")}</td>
                <td class="value">{I18n.t("format.creationDate", formatCreateDate($user.created, true))}</td>
            </tr>
        {/if}
        <tr>
            <td>
                <Button href="/myconext/api/sp/personal"
                        onClick={() => showModal = true}
                        large={true}
                        label={I18n.t("account.data")}/>
            </td>
            <td>{I18n.t("account.personalInfo")}</td>
        </tr>
        <tr>
            <td>
                <Button onClick={() => navigate("/delete-account")}
                        large={true}
                        label={I18n.t("account.delete")}/>
            </td>
            <td>{I18n.t("account.deleteInfo")}</td>
        </tr>
        </tbody>
    </table>


</div>

{#if showModal}
    <Modal submit={() => setTimeout(() => showModal = false, 750)}
           cancel={() => showModal = false}
           download={personalDataFileName()}
           href="/myconext/api/sp/personal"
           question={I18n.t("account.downloadDataConfirmation")}
           title={I18n.t("account.downloadData")}
           confirmTitle={I18n.t("account.downloadData")}>
    </Modal>
{/if}