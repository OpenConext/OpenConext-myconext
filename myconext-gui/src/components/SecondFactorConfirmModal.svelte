<script>
    import Modal from "./Modal.svelte";
    import TiqrAuthentication from "./TiqrAuthentication.svelte";
    import {confirmStepUp} from "../api";
    import {authenticationStatus} from "../constants/authenticationStatus";
    import I18n from "../locale/I18n";

    export let onConfirmed;
    export let onClose;
    export let title = I18n.t("UseApp.ConfirmSecondFactor");

    const handleDone = ({status, sessionKey}) => {
        onClose();
        if (status === authenticationStatus.SUCCESS) {
            confirmStepUp(sessionKey).then(() => onConfirmed(sessionKey));
        }
    }
</script>

<style lang="scss">
    .slot {
        display: flex;
        flex-direction: column;
    }
</style>

<Modal cancel={onClose}
       warning={true}
       title={title}>
    <div class="slot">
        <TiqrAuthentication onDone={handleDone}/>
    </div>
</Modal>
