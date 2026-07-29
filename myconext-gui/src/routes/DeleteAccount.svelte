<script>
    import {config, user} from "../stores/user";
    import {get} from "svelte/store";
    import I18n from "../locale/I18n";
    import {navigate} from "svelte-routing";
    import critical from "../icons/critical.svg?raw";

    import Modal from '../components/Modal.svelte';
    import {deleteUser, confirmStepUp} from "../api";
    import Button from "../components/Button.svelte";
    import {isEmpty} from "../utils/utils";
    import TiqrAuthentication from "../components/TiqrAuthentication.svelte";
    import {authenticationStatus} from "../constants/authenticationStatus.js";

    let showModal = false;
    let show2ndFactorModal = false;

    let name = "";

    const disableDeleteButton = input => {
        const inputSanitized = isEmpty(input) ? "" : input.toLowerCase().replace(/["']/g, "");
        return inputSanitized !== "delete" && inputSanitized !== "verwijder";
    }

    const has2ndFactor = () => {
        const currentUser = get(user);
        const options = currentUser.loginOptions || [];
        return options.includes("useApp");
    }

    const startDeleteUserFlow = () => {
        if(has2ndFactor()) {
            show2ndFactorModal = true;
        }
        else {
            doDeleteUser();
        }
    }

    const handle2ndFactor = ({status, sessionKey}) => {
        console.log(status, sessionKey);
        if (status === authenticationStatus.SUCCESS) {
            confirmStepUp(sessionKey).then(() => doDeleteUser());
        }
    }

    const doDeleteUser = () => {
        deleteUser().then(() => {
            $user = {
                id: "",
                email: "",
                givenName: "",
                familyName: "",
                usePassword: false
            };
            window.location.href = `${$config.accountBaseUrl}/doLogout?param=${encodeURIComponent("delete=true")}`;
        });
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
        font-weight: 600;
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

    <h2>{I18n.t("Account.TitleDelete.COPY")}</h2>
    <p class="divider">{I18n.t("Account.Info1.COPY")}</p>
    <p class="divider">{I18n.t("Account.Info2.COPY")}</p>
    <p class="divider">{I18n.t("Account.Info3.COPY")}</p>
    <p class="divider">{I18n.t("Account.Info4.COPY")}</p>
    <div class="options">
        <Button href="/cancel" label={I18n.t("YourVerifiedInformation.ConfirmRemoval.Button.Cancel.COPY")}
                onClick={() => navigate("/account")} className="cancel"/>
        <Button href="/delete" label={I18n.t("Account.Delete.COPY")}
                large={true}
                onClick={() => showModal = true}/>
    </div>
</div>

{#if showModal || show2ndFactorModal}
    {#if show2ndFactorModal}
        <Modal cancel={() => show2ndFactorModal = false}
               warning={true}
               title="Confirm 2nd factor">
            <div class="slot">
                <TiqrAuthentication onDone={handle2ndFactor}/>
            </div>
        </Modal>
    {:else}
        <Modal submit={() => startDeleteUserFlow()}
               cancel={() => showModal = false}
               warning={true}
               confirmTitle={I18n.t("YourVerifiedInformation.ConfirmRemoval.Button.YesDelete.COPY")}
               disableSubmit={disableDeleteButton(name)}
               title={I18n.t("ConfirmDelete.Title.COPY")}>
            <div class="slot">
                <div class="warning-box">
                    <span>{@html critical}</span>
                    <span>{I18n.t("ConfirmDelete.Disclaimer.COPY")}</span>
                </div>
                <p>{I18n.t("Account.Proceed.COPY")}</p>
                <label for="name">{I18n.t("account.confirmation")}</label>
                <input id="name"
                       placeholder={I18n.t("Profile.RemoveServicePrompt.Delete.COPY")}
                       type="text"
                       spellcheck="false"
                       bind:value={name}/>
            </div>
        </Modal>
    {/if}
{/if}
