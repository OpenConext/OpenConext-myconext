<script>
    export let onClick;
    export let text;
</script>

<style>
    button {
        border-radius: 4px;
        padding: 10px 20px;
        display: inline-block;
        font-size: larger;
        background-color: #5da7c5;
        color: whitesmoke;
    }

</style>

<button on:click={onClick}>{text}</button>
