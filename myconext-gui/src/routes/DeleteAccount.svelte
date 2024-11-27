<script>
    import {config, user} from "../stores/user";
    import I18n from "../locale/I18n";
    import {navigate} from "svelte-routing";
    import critical from "../icons/critical.svg";

    import Modal from '../components/Modal.svelte';
    import {deleteUser} from "../api";
    import Button from "../components/Button.svelte";
    import {isEmpty} from "../utils/utils";

    let showModal = false;

    let name = "";

    const disableDeleteButton = input => {
        const inputSanitized = isEmpty(input) ? "" : input.toLowerCase().replace(/["']/g, "");
        return inputSanitized !== "delete" && inputSanitized !== "verwijder";
    }

    const deleteUserAction = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true
        } else {
            deleteUser().then(() => {
                $user = {
                    id: "",
                    email: "",
                    givenName: "",
                    familyName: "",
                    usePassword: false
                };
                window.location.href = `${$config.idpBaseUrl}/doLogout?param=${encodeURIComponent("delete=true")}`;
            });
        }
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

    p.divider {
        line-height: 1.33;
        letter-spacing: normal;
        font-weight: 300;
        margin-bottom: 22px;
    }

    div.warning-box {
        margin: 25px 0;
        display: flex;
        align-items: center;
        background-color: #eaa1a1;
        padding: 20px;
        border-radius: 8px;

        span:last-child {
            margin-left: 15px;
        }
    }

    label {
        font-weight: bold;
        margin: 33px 0 13px 0;
        display: inline-block;
    }

    input {
        border-radius: 8px;
        border: solid 1px #676767;
        padding: 14px;
        font-size: 16px;
    }

    .options {
        margin: 5px 0 40px 0;
        display: flex;
        justify-content: flex-end;
    }

    .slot {
        display: flex;
        flex-direction: column;
    }


</style>

<div class="account">

    <h2>{I18n.t("account.titleDelete")}</h2>
    {#each [1, 2, 3, 4] as i}
        <p class="divider">{I18n.t("account.info" + i)}</p>
    {/each}
    <div class="options">
        <Button href="/cancel" label={I18n.t("account.cancel")}
                onClick={() => navigate("/account")} className="cancel"/>
        <Button href="/delete" label={I18n.t("account.deleteAccount")}
                large={true}
                onClick={deleteUserAction(true)}/>
    </div>
</div>

{#if showModal}
    <Modal submit={deleteUserAction(false)}
           cancel={() => showModal = false}
           warning={true}
           confirmTitle={I18n.t("modal.delete")}
           disableSubmit={disableDeleteButton(name)}
           title={I18n.t("account.deleteAccountSure")}>
        <div class="slot">
            <div class="warning-box">
                <span>{@html critical}</span>
                <span>{I18n.t("account.deleteAccountWarning")}</span>
            </div>
            <p>{I18n.t("account.proceed")}</p>
            <label for="name">{I18n.t("account.confirmation")}</label>
            <input id="name"
                   placeholder={I18n.t("account.confirmationPlaceholder")}
                   type="text"
                   spellcheck="false"
                   bind:value={name}/>
        </div>
    </Modal>
{/if}