<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {fetchServiceNameById, knownAccount} from "../api";
    import phone from "../icons/redesign/undraw_mobile_interface_re_1vv9.svg";
    import ButtonContainer from "../components/ButtonContainer.svelte";
    import ImageContainer from "../components/ImageContainer.svelte";
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

<style>

    h2 {
        color: var(--color-primary-green);
    }

    p.explanation {
        margin: 15px 0;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}

<h2 class="header">{@html DOMPurify.sanitize(I18n.t("appRequired.header", {service: serviceName}))} </h2>
<ImageContainer icon={phone}/>
<p class="explanation">{@html I18n.t("appRequired.info")}</p>
<ButtonContainer>
    <Button className="cancel"
            href={I18n.t("appRequired.cancel")}
            onClick={() => cancel(true)}
            label={I18n.t("appRequired.no")}/>
    <Button href={I18n.t("appRequired.yesLink")} onClick={() => navigate(`/getapp?id=${id}`)}
            label={I18n.t("appRequired.yes")}/>
</ButtonContainer>
{#if showModal}
    <Modal submit={() => cancel(false)}
           cancel={() => showModal = false}
           question={I18n.t("appRequired.warning", {service: serviceName})}
           title={I18n.t("appRequired.warningTitle")}
           confirmTitle={I18n.t("appRequired.warningTitle")}>
    </Modal>
{/if}

