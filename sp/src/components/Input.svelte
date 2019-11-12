<script>
    export let bindValue;
    export let value;
    export let placeholder = "";
</script>

<style>
    input {
        border: 1px solid #dadce0;
        border-radius: 4px;
        padding: 10px;
        font-size: larger;
        width: 90%;
        margin: 20px 10px;
    }
</style>

<input type="text" bind:value={bindValue} value={value} placeholder={placeholder}/>