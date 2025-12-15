<script>
    import {link, navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import I18n from "../locale/I18n";
    import Modal from "../components/Modal.svelte";
    import Button from "./Button.svelte";

    export let question = null;
    export let preLink = null;
    export let linkText = null;
    export let route = null;
    export let href = null;
    export let isMfa = false;
    export let interContent = false;

    export let showButton = false;

    let isMfaParameter = false;
    let showModal = false;
    let userOverride = false;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        isMfaParameter = urlSearchParams.has("mfa");
    })

    const mfaWarning = confirmation => {
        if (confirmation) {
            showModal = true;
        } else {
            navigate(route);
        }
    }

</script>

<style lang="scss">
    .sub-content {
        display: flex;
        flex-direction: column;
        position: relative;
        margin: 16px auto 0 auto;
        padding: 22px 32px;
        background-color: white;
        width: var(--width-app);
        justify-content: center;
        border-radius: 4px;
        box-shadow: 0 3px 0 2px #003980;
    }

    .inter-content {
      display: flex;
      flex-direction: column;
      margin: 16px auto 0 auto;
      padding: 22px 32px;
      background-color: white;
      justify-content: center;
    }

    .show-button {
      margin: 0;
      padding: 0;
      .sub-content-inner {
        display: block;
      }
    }

    @media (max-width: 800px) {
        .sub-content {
            padding: 32px 28px;
            width: 100%;
            border-radius: 0;
            box-shadow: none;
        }
    }

    .sub-content-inner {
        display: flex;
    }

    span.question {
        font-weight: 600;
    }

    span.pre-link {
        font-weight: normal;
    }

    a {
        color: var(--color-primary-blue);
        text-decoration: underline;
        font-weight: normal;

        &:hover {
            color: var(--color-hover-blue);
            font-weight: 600;
        }
    }

</style>
<div class="{interContent ? 'inter-content' : 'sub-content'} {showButton ? 'show-button' : ''}">
    <div class="sub-content-inner">
    <span class="question">{@html question}
        {#if preLink}
            <span class="pre-link">{preLink}</span>
        {/if}
        {#if !showButton && route && (userOverride || !isMfaParameter || !isMfa)}
            <a href={route} use:link>
                {linkText}
            </a>
        {:else if showButton && route && (userOverride || !isMfaParameter || !isMfa)}
            <Button href="{route}" useLink="true" label={linkText} className="full" big="true"/>
        {:else if !showButton && route && !userOverride && isMfaParameter && isMfa}
            <a href={route}
               on:click|preventDefault|stopPropagation={() => showModal = true}>
                {linkText}
            </a>
        {:else if showButton && route && !userOverride && isMfaParameter && isMfa}
            <Button href="{route}" label={linkText} onClick="{() => showModal = true}" className="full"/>
        {:else}
            <a href={href} target="_blank">{linkText}</a>
        {/if}
    </span>
    </div>
</div>
{#if showModal}
    <Modal submit={() => showModal = false}
           cancel={() => mfaWarning(false)}
           question={I18n.t("SubContent.Warning.COPY", {service: "test"})}
           title={I18n.t("SubContent.WarningTitle.COPY")}
           cancelLabel={I18n.t("SubContent.ConfirmLabel.COPY")}
           confirmLabel={I18n.t("SubContent.CancelLabel.COPY")}>
    </Modal>
{/if}

