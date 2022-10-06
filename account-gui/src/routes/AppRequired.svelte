<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {fetchServiceNameById, knownAccount} from "../api";
    import ButtonContainer from "../components/ButtonContainer.svelte";
    import {navigate} from "svelte-routing";
    import {user} from "../stores/user";
    import Modal from "../components/Modal.svelte";
    import DOMPurify from "dompurify";

    export let id;
    let serviceName = null;
    let showSpinner = true;
    let showModal = false;

    onMount(() => {
        $links.displayBackArrow = false;

        fetchServiceNameById(id).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const submit = () => {
        navigate(`/getapp?id=${id}`);
    }

    const cancel = showWarning => {
        if (showWarning) {
            showModal = true;
        } else {
            showSpinner = true;
            showModal = false;
            knownAccount($user.knownUser || $user.email).then(res => {
                if ($user.preferredLogin && res.includes($user.preferredLogin)) {
                    navigate(`/${$user.preferredLogin.toLowerCase()}/${id}`);
                } else {
                    //By contract the list ordered from more secure to less secure
                    navigate(`/${res[0].toLowerCase()}/${id}`);
                }
            });
        }
    }

</script>

<style lang="scss">

    h2 {
        color: var(--color-primary-green);
        margin-bottom: 10px;
    }

    p.explanation {
        margin: 15px 0;

        &.last {
            margin-bottom: 40px;
        }
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}

<h2 class="header">{@html DOMPurify.sanitize(I18n.t("appRequired.header"))} </h2>
<p class="explanation">{@html I18n.t("appRequired.info", {service: serviceName})}</p>
<p class="explanation last">{@html I18n.t("appRequired.info2")}</p>
<ButtonContainer>
    <Button className="cancel"
            href={I18n.t("appRequired.cancel")}
            onClick={() => cancel(true)}
            label={I18n.t("appRequired.no")}/>
    <Button href={I18n.t("appRequired.yesLink")}
            onClick={submit}
            label={I18n.t("appRequired.yes")}/>
</ButtonContainer>
{#if showModal}
    <Modal submit={submit}
           cancel={() => cancel(false)}
           question={I18n.t("appRequired.warning", {service: serviceName})}
           title={I18n.t("appRequired.warningTitle")}
           cancelLabel={I18n.t("appRequired.cancelLabel")}
           confirmLabel={I18n.t("appRequired.confirmLabel")}>
    </Modal>
{/if}

