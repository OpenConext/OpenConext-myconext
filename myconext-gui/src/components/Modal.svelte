<script>
    import I18n from "i18n-js";
    import Button from "./Button.svelte";

    export let submit;
    export let cancel;
    export let title;
    export let question;

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
    <div class="modal-header">
        <h3>{title}</h3>
    </div>
    <div class="modal-body">
        <p>{question}</p>
    </div>

    <div class="options">
        <Button className="cancel" onClick={cancel}
                label={I18n.ts("modal.cancel")}/>

        <Button onClick={submit}
                label={I18n.ts("modal.confirm")}/>
    </div>
</div>

<style>
    .modal-background {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 57, 128, 0.8);
        z-index: 999;
    }

    .modal {
        z-index: 9999;
        position: absolute;
        left: 50%;
        top: 35%;
        width: calc(100vw - 4em);
        max-width: 32em;
        max-height: calc(100vh - 4em);
        overflow: auto;
        transform: translate(-50%, -50%);

        border-radius: 8px;
        background: white;
    }
    .modal-header {
        padding: 18px 32px;
        background-color: #dfe3e8;
    }

    .modal-body {
        padding: 18px 32px;
    }
    div.options {
        padding: 18px ;
    }
</style>
