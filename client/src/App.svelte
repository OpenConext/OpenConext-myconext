<script>
    import {Route, Router} from "svelte-routing";
    import Login from "./routes/Login.svelte";
    import MagicLink from "./routes/MagicLink.svelte";

    export let url = "";
</script>

<style>
</style>

<Router url="{url}">
    <Route path="/login/:id" let:params>
        <Login id="{params.id}"></Login>
    </Route>
    <Route path="/magic" component={MagicLink}/>
</Router>
