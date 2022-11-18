<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {conf, links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import Spinner from "../components/Spinner.svelte";
    import {fetchServiceNameByHash} from "../api";
    import ButtonContainer from "../components/ButtonContainer.svelte";
    import {navigate} from "svelte-routing";
    import Modal from "../components/Modal.svelte";
    import DOMPurify from "dompurify";
    import Verification from "../components/Verification.svelte";
    import {proceed} from "../utils/sso";

    let hash;
    let serviceName = null;
    let showSpinner = true;
    let showModal = false;

    onMount(() => {
        $links.displayBackArrow = false;

        const urlSearchParams = new URLSearchParams(window.location.search);
        hash = urlSearchParams.get("h");

        fetchServiceNameByHash(hash).then(res => {
            serviceName = res.name;
            showSpinner = false;
        });
    });

    const submit = () => {
        navigate(`/getapp?h=${hash}`);
    }

    const cancel = showWarning => {
        if (showWarning) {
            showModal = true;
        } else {
            showSpinner = true;
            showModal = false;
            proceed($conf.magicLinkUrl);
        }
    }

</script>

<style lang="scss">

    h2 {
        margin: 10px 0 30px 0;
        font-size: 32px;
        color: var(--color-primary-green);
    }

    p.explanation {
        margin-bottom: 25px;

        &.last {
            margin-bottom: 40px;
        }
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}

<h2>{I18n.t("stepup.header")}</h2>
<p class="explanation">{@html I18n.t("stepup.info", {name: DOMPurify.sanitize(serviceName)})}</p>
<Verification explanationText={I18n.t("appRequired.info", {service: DOMPurify.sanitize(serviceName)})} verified={false}/>
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

