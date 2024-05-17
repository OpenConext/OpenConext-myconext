<script>
    import I18n from "i18n-js";
    import Button from "./Button.svelte";
    import DOMPurify from "dompurify";
    import {onDestroy, onMount} from "svelte";
    import closeIcon from "../icons/close_smll.svg";

    export let submit;
    export let cancel;
    export let close;
    export let title;
    export let question;
    export let warning = false;
    export let showOptions = true;
    export let controlBody = false;
    export let confirmTitle = I18n.t("modal.confirm");
    export let cancelTitle = I18n.t("modal.cancel");
    export let disableSubmit = false;
    export let download = undefined;
    export let href = undefined;

    let modal;

    const handleKeydown = e => {
        if (e.key === "Escape") {
            if (cancel) {
                cancel();
            }
            if (close) {
                close();
            }
        }
    };

    onMount(() => {
        document.body.classList.add("modal-open");
    })

    onDestroy(() => {
        document.body.classList.remove("modal-open");
    })

</script>

<style lang="scss">
    .modal {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(146, 151, 158, 0.7);
        z-index: 999;
        display: flex;
    }

    .modal-content {
        margin: auto;
        width: calc(100vw - 4em);
        max-width: 32em;
        max-height: calc(100vh - 4em);
        border-radius: 8px;
        background: white;
        overflow: scroll;
    }

    .modal-header {
        padding: 18px 32px;
        background-color: var(--color-primary-blue);
        color: white;
        border-top-left-radius: 8px;
        border-top-right-radius: 8px;
        position: relative;

        span {
            position: absolute;
            right: 0;
            top: 0;
            padding: 12px;
            cursor: pointer;

            :global(svg) {
                fill: white
            }
        }
    }

    .modal-header.warning {
        background-color: var(--color-warning-red);
        color: white;
    }

    .modal-body {
        padding: 18px 32px;

        &.control-body {
            padding: 0;
        }
    }

    div.options {
        padding: 18px;
        display: flex;
        text-align: center;
        justify-content: flex-end;
    }
</style>

<svelte:window on:keydown={handleKeydown}/>

<div class="modal">
    <div class="modal-content">
        {#if title}
        <div class="modal-header" class:warning>
            <h3>{title}</h3>
            <span on:click={() => handleKeydown({key:"Escape"})}>
                {@html closeIcon}
            </span>
        </div>
        {/if}
        <div class="modal-body" class:control-body={controlBody}>
            {#if question}
                <p>{@html DOMPurify.sanitize(question)}</p>
            {/if}
            <slot></slot>
        </div>
        {#if showOptions}
            <div class="options">
                {#if cancel}
                    <Button className="cancel"
                            onClick={cancel}
                            small={true}
                            label={cancelTitle}/>
                {/if}
                {#if submit}
                    <Button onClick={submit}
                            warning={warning}
                            href={href}
                            small={true}
                            download={download}
                            disabled={disableSubmit}
                            label={confirmTitle}/>
                {/if}
            </div>
        {/if}
    </div>
</div>
