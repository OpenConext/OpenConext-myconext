<script>
    import {Route, Router} from "svelte-routing";
    import Login from "./routes/Login.svelte";
    import SessionLost from "./routes/SessionLost.svelte";
    import MagicLink from "./routes/MagicLink.svelte";
    import Confirm from "./routes/Confirm.svelte";
    import Header from "./components/Header.svelte";
    import Footer from "./components/Footer.svelte";

    export let url = "";
</script>

<style>
    div.idp {
        display: flex;
        flex-direction: column;
        height: 100%;
        background-color: #fafafa;
    }
</style>
<div class="idp">
    <Header />
    <Router url="{url}">
        <Route path="/login/:id" let:params>
            <Login id="{params.id}"></Login>
        </Route>
        <Route path="/magic" component={MagicLink}/>
        <Route path="/confirm" component={Confirm}/>
        <Route path="/session" component={SessionLost}/>
    </Router>
    <Footer />
</div>
