<script>
    import {Route, Router} from "svelte-routing";
    import Login from "./routes/Login.svelte";

    export let url = "";
</script>

<style>
</style>

<Router url="{url}">
    <!--    <nav>-->
    <!--        <Link to="/">Login</Link>-->
    <!--    </nav>-->
    <Route path="/" component="{Login}"/>
</Router>
