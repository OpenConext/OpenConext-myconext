<script>
    import I18n from "i18n-js";
    import Button from "./Button.svelte";
    import DOMPurify from "dompurify";

    export let submit;
    export let cancel;
    export let title;
    export let question;
    export let warning = false;
    export let confirmTitle = I18n.t("modal.confirm");
    export let disableSubmit = false;
    export let download = undefined;
    export let href = undefined;

    let modal;

    const handle_keydown = e => {
        if (e.key === "Escape") {
            cancel();
        }
    };

</script>

<style>
    .modal {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 57, 128, 0.8);
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
    }

    .modal-header {
        padding: 18px 32px;
        background-color: #dfe3e8;
        border-top-left-radius: 8px;
        border-top-right-radius: 8px;
    }

    .modal-header.warning {
        background-color: var(--color-warning-red);
        color: white;
    }

    .modal-body {
        padding: 18px 32px;
    }

    div.options {
        padding: 18px;
        display: flex;
        text-align: center;
        justify-content: flex-end;
    }
</style>

<svelte:window on:keydown={handle_keydown}/>

<div class="modal">
    <div class="modal-content">
        <div class="modal-header" class:warning>
            <h3>{title}</h3>
        </div>

        <div class="modal-body">
            {#if question}
                <p>{@html DOMPurify.sanitize(question)}</p>
            {/if}
            <slot></slot>
        </div>

        <div class="options">
            <Button className="cancel" onClick={cancel}
                    label={I18n.t("modal.cancel")}/>

            <Button onClick={submit}
                    warning={warning}
                    href={href}
                    download={download}
                    disabled={disableSubmit}
                    label={confirmTitle}/>
        </div>
    </div>
</div>
