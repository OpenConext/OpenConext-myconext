<script>
    import {onMount} from "svelte";
    import ToDo from "./ToDo.svelte"
    import Widget from "./Widget.svelte";
    import {user} from "./stores/store"
    import Comp from "./Comp.svelte";
    let todoList = [];
    let counter = 1;

    onMount(async () => {
        const res = await fetch(`http://localhost:8080/api/todo`);
        todoList = await res.json();
    });
    let newToDo = {done: false, description: "", show: false};

</script>

<style>
    .todo-container {
        display: flex;
        flex-direction: column;
        justify-content: left;
    }

    button {
        display: inline-block;
        margin: 25px;
        padding: 3px 10px;
        color: blue;
    }
    input.new {
        padding: 5px;
        margin-top: 15px;
        max-width: 400px;
    }
</style>

<div class="todo-container">
    {#each todoList as todo}
        <ToDo todo={todo}></ToDo>
    {:else}
    <!-- this block renders when todoList.length === 0 -->
        <p>Loading...</p>
    {/each}
    {#if newToDo.show}
        <input class="new" type="text" bind:value={newToDo.description}>
    {/if}
</div>
<button on:click="{() => newToDo.show = true}">New ToDo</button>


<Widget>
    <h3 slot="header">Hello</h3>
    <p slot="footer">Copyright (c) 2019 Zilverline</p>
</Widget>

<p>{$user.name}</p>
<p>{$user.attributes.nested.value1}</p>
<input bind:value={$user.attributes.nested.value1}>
<p>{$user.attributes.l[1]}</p>
<ul>
    {#each $user.attributes.l as item}
        <li>{item}</li>
    {/each}
</ul>
<button on:click="{() => user.update(value => {
    counter = counter + 3;
    value.attributes.l.push(counter);
    return value;
})}">Add</button>
<p>comp</p>
<Comp/>