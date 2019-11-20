<script>
    import {flash} from "../stores/user";

    const clearFlash = () => {$flash = "";
    }
</script>

<style>
    .flash {
        color: white;

        position: absolute;
        width: 100%;
        top: 0;
        z-index: 6;
        opacity: .9;
        background-color: green;
        transition: top 150ms ease-in-out;
    }

    .flash.hide {
        transition: top 1000ms ease-in-out;
        top: -80px;
    }

    .message-container {
        max-width: 1180px;
        margin: 0 auto;
        position: relative;
    }

    .message-container p {
        padding: 10px 25px 10px 15px;
        font-weight: bold;
    }

    a.close {
        text-decoration: none;
        position: absolute;
        right: 10px;
        top: -5px;
        color: white;
        font-size: larger;
        padding: 6px;
    }

</style>

<div class="flash" class:hide={!$flash}>
    <div class="message-container">
        <p>{$flash}
            <a href="/close" class="close" on:click|preventDefault|stopPropagation={clearFlash}>
                x
            </a>
        </p>
    </div>
</div>