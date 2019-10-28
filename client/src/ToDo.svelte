<script>
    export let todo;

    const className = todo => todo.done ? "done" : "not-done";

    const update = async () => {
        const toSave = {...todo, done:!todo.done};
        const res = await fetch(`http://localhost:8080/api/todo`, {method:"PUT", headers: {"Content-Type": "application/json"}, body: JSON.stringify(toSave)});
        todo = await res.json();
    }

</script>

<style>
    label {
        display: inline-block;
    }

    label.not-done {
        color: grey;
    }
    label.done {
        color: green;
    }
</style>

<div class="todo">
    <input id="{todo.id}" type="checkbox" bind:checked={todo.done} on:click={update}>
    <label for="{todo.id}" class={className(todo)}>{todo.description}</label>
</div>
