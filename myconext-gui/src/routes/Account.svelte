<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";

    import Modal from '../components/Modal.svelte';
    import {deleteUser} from "../api";

    let showModal = false;

    const deleteUserAction = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true
        } else {
            deleteUser($user).then(() => {
                $user = {
                    id: "",
                    email: "",
                    givenName: "",
                    familyName: "",
                    guest: true,
                    usePassword: false
                };
                navigate("/landing");
            });
        }
    }

</script>

<style>
    .account {
        width: 100%;
    }

    .inner {
        max-width: 1080px;
        margin: 0 auto;
        padding: 15px 0;
        font-size: 18px;
        display: flex;
        flex-direction: column;
    }

    h2 {
        font-size: 22px;
        margin-bottom: 20px;
    }

    p {
        font-weight: 300;
        margin-bottom: 20px;
    }

    label {
        font-weight: 300;
        margin-right: 20px;
        width: 120px;
    }

    input[type=text] {
        border: 1px solid #dadce0;
        border-radius: 4px;
        font-weight: 300;
        padding: 6px;
        margin: 5px 0;
        font-size: 18px;
        flex-grow: 2;
    }

    div.form-field {
        display: flex;
        width: 100%;
        align-items: center;
        margin-bottom: 15px;
        position: relative;
    }

    .button {
        border: 1px solid #a6000b;
        width: 260px;
        background-color: #c7000b;
        border-radius: 2px;
        padding: 10px 20px;
        margin-top: 15px;
        display: inline-block;
        color: white;
        text-decoration: none;
        cursor: pointer;
        text-align: center;
    }

    @media (max-width: 720px) {
        div.form-field {
            flex-direction: column;
            align-items: flex-start;
        }

    }


</style>

<div class="account">
    <div class="inner">
        <h2>{I18n.ts("account.title")}</h2>
        <div class="form-field">
            <label for="email">{I18n.ts("security.useMagicLink")}</label>
            <input id="email" type="text" value={$user.email} disabled>
        </div>
        <div class="options">
            <a class="button" href="/delete"
               on:click|preventDefault|stopPropagation={deleteUserAction(true)}>
                {I18n.ts("account.deleteAccount")}
            </a>
        </div>

    </div>

</div>

{#if showModal}
    <Modal submit={deleteUserAction(false)} cancel={() => showModal = false}>
        <h2 slot="header">
            {I18n.ts("account.deleteAccount")}
        </h2>
        <p slot="body">
            {I18n.ts("account.deleteAccountConfirmation")}
        </p>
    </Modal>
{/if}