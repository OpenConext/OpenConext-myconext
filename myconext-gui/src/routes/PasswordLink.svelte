<script>
    import {flash, user} from "../stores/user";
    import I18n from "../locale/I18n";
    import {outstandingEmailLinks, resetPasswordLink} from "../api";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import Modal from "../components/Modal.svelte";

    const usePassword = $user.usePassword;
    let outstandingEmailReset = false;
    let initial = true;

    onMount(() => {
        outstandingEmailLinks().then(res => {
            outstandingEmailReset = res;
        })
    });

    const cancel = () => navigate("/security");

    const proceed = () => {
        if (initial && outstandingEmailReset) {
            initial = false;
        } else {
            resetPasswordLink().then(() => {
                navigate("/security");
                flash.setValue(I18n.t("password.flash.passwordLink", {name: $user.email}));
            })
        }
    }

</script>

<style lang="scss">
    .password {
        width: 100%;
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    h2 {
        margin-top: 35px;
        color: var(--color-primary-green);
    }

    p.info {
        margin: 22px 0 0 0;
    }

    .options {
        margin: 60px 0 20px 0;
        display: flex;
    }

</style>
<div class="password">
    <h2>{usePassword ? I18n.t("password.updateTitle") : I18n.t("password.addTitle")}</h2>
    <p class="info">{@html usePassword ? I18n.t("password.updateInfo") : I18n.t("password.addInfo")}</p>
    <div class="options">
        <Button className="cancel" label={I18n.t("password.cancel")} onClick={cancel}/>

        <Button label={I18n.t("modal.confirm")}
                onClick={proceed}/>
    </div>
</div>
{#if outstandingEmailReset && !initial}
    <Modal submit={proceed}
           cancel={() => navigate("/security")}
           warning={true}
           question={I18n.t("password.outstandingEmailResetConfirmation")}
           title={I18n.t("password.outstandingEmailReset")}>
    </Modal>
{/if}