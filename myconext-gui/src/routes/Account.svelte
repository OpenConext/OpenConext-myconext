<script>
    import {user} from "../stores/user";
    import {formatCreateDate} from "../format/date";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";

    import Modal from '../components/Modal.svelte';
    import {deleteUser} from "../api";
    import Button from "../components/Button.svelte";

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
                navigate("/landing?delete=true");
            });
        }
    }

</script>

<style>
    .account {
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
        margin-bottom: 40px;
    }

    h3 {
        margin-bottom: 10px;
    }

    p {
        font-size: 18px;
        line-height: 1.33;
        letter-spacing: normal;
    }

    p.divider {
        margin-bottom: 22px;
    }

    .options {
        margin: 30px 0;
    }

    :global(a svg.menu-link) {
        fill: var(--color-primary-green);
    }


</style>

<div class="account">

    <div class="inner">
        <h2>{I18n.t("account.title")}</h2>
        <p class="info">{formatCreateDate($user.created)}</p>
        <h3>{I18n.t("account.deleteTitle")}</h3>
        {#each [1,2,3,4] as i}
            <p class="divider">{I18n.t("account.info"+i)}</p>
        {/each}
        <div class="options">
            <Button href="/delete" label={I18n.ts("account.deleteAccount")}
                    onClick={deleteUserAction(true)} className="full cancel"/>
        </div>
    </div>

</div>

{#if showModal}
    <Modal submit={deleteUserAction(false)}
           cancel={() => showModal = false}
           question={I18n.ts("account.deleteAccountConfirmation")}
                   title={I18n.ts("account.deleteAccount")}></Modal>
{/if}