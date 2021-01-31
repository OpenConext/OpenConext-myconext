<script>
    import {user} from "../stores/user";
    import {formatCreateDate} from "../format/date";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import critical from "../icons/critical.svg";

    import Modal from '../components/Modal.svelte';
    import {deleteUser} from "../api";
    import Button from "../components/Button.svelte";

    let showModal = false;

    let name = "";

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
                    guest: true,
                    usePassword: false
                };
                navigate("/landing?delete=true");
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

    p.info {
        line-height: 1.33;
        letter-spacing: normal;
        font-weight: 300;
        margin-bottom: 26px;
    }

    p.divider {
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
                onClick={() => navigate("/account")} small={true} className="cancel"/>
        <Button href="/delete" label={I18n.t("account.deleteAccount")}
                onClick={deleteUserAction(true)} className="full"/>
    </div>
</div>

{#if showModal}
    <Modal submit={deleteUserAction(false)}
           cancel={() => showModal = false}
           warning={true}
           disableSubmit={name !== `${$user.givenName} ${$user.familyName}`}
           title={I18n.t("account.deleteAccountSure")}>
        <div class="slot">
            <div class="warning-box">
                <span>{@html critical}</span>
                <span>{I18n.t("account.deleteAccountWarning")}</span>
            </div>
            <p>{I18n.t("account.proceed")}</p>
            <label for="name">{I18n.t("account.name")}</label>
            <input id="name" placeholder={I18n.t("account.namePlaceholder")} type="text" bind:value={name}/>
        </div>
    </Modal>
{/if}