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
        background-color: dodgerblue;
        color: whitesmoke;
    }

</style>

<button on:click={onClick}>{text}</button>
