<script>
    import {flash} from "../stores/user";
    import closeIcon from "../icons/close_smll.svg"

</script>

<style>
    .flash {
        color: white;
        position: absolute;
        width: 100%;
        height: 28px;
        z-index: 6;
        opacity: .7;
        left: 0;
        background-color: green;
        transition: height 650ms ease-in-out;
    }

    .flash.hide {
        transition: height 650ms ease-in-out;
        height: 0;
    }

    .message-container {
        max-width: 1180px;
        margin: 0 auto;
        position: relative;
    }

    .message-container p {
        padding: 2px 25px 0 15px;
        font-weight: 300;
        font-size: 14px;
    }

    span.close {
        text-decoration: none;
        position: absolute;
        right: 10px;
        top: 0;
        color: white;
        cursor: pointer;
    }

    :global(span.close svg) {
        fill: white;
        width: 20px;
    }

</style>

<div class="flash" class:hide={!$flash}>
    <div class="message-container">
        <p>{$flash}
            <span href="/close" class="close" on:click|preventDefault|stopPropagation={() => flash.setValue("")}>
                {@html closeIcon}
            </span>
        </p>
    </div>
</div>