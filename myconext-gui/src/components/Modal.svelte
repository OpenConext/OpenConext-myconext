<script>
    import I18n from "i18n-js";

    export let submit;
    export let cancel;

    let modal;

    const handle_keydown = e => {
        if (e.key === "Escape") {
            cancel();
        }
    };

</script>

<svelte:window on:keydown={handle_keydown}/>

<div class="modal-background" on:click={cancel}></div>

<div class="modal" role="dialog" aria-modal="true" bind:this={modal}>
    <slot name="header"></slot>
    <slot name="body"></slot>
    <div class="options">
        <a class="button" href="/cancel"
           on:click|preventDefault|stopPropagation={cancel}>
            {I18n.t("modal.cancel")}
        </a>
        <a class="button" href="/delete"
           on:click|preventDefault|stopPropagation={submit}>
            {I18n.t("modal.confirm")}
        </a>
    </div>
</div>

<style>
    .modal-background {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.3);
    }

    .modal {
        position: absolute;
        left: 50%;
        top: 50%;
        width: calc(100vw - 4em);
        max-width: 32em;
        max-height: calc(100vh - 4em);
        overflow: auto;
        transform: translate(-50%, -50%);
        padding: 1em;
        border-radius: 0.2em;
        background: white;
    }
    .button {
        border: 1px solid #818181;
        width: 100%;
        background-color: #c7c7c7;
        border-radius: 2px;
        padding: 10px 20px;
        display: inline-block;
        color: black;
        text-decoration: none;
        cursor: pointer;
        text-align: center;
    }

    .button:last-child {
        margin-left: 95px;
    }

    div.options {
        display: flex;
        margin-top: 60px;
        align-content: space-between;
    }
</style>
