<script>
    import {Route, Router} from "svelte-routing";
    import Login from "./routes/Login.svelte";
    import SessionLost from "./routes/SessionLost.svelte";
    import MagicLink from "./routes/MagicLink.svelte";
    import Confirm from "./routes/Confirm.svelte";
    import LinkExpired from "./routes/LinkExpired.svelte";
    import NotFound from "./routes/NotFound.svelte";
    import Header from "./components/Header.svelte";
    import Footer from "./components/Footer.svelte";

    export let url = "";
</script>

<style>
    div.idp {
        display: flex;
        flex-direction: column;
        height: 100%;
        background-color: #4db3cf;
    }
</style>
<div class="idp">
    <Header />
    <Router url="{url}">
        <Route path="/login/:id" let:params>
            <Login id="{params.id}"></Login>
        </Route>
        <Route path="/magic/:id" let:params>
            <MagicLink id="{params.id}"></MagicLink>
        </Route>
        <Route path="/confirm" component={Confirm}/>
        <Route path="/session" component={SessionLost}/>
        <Route path="/expired" component={LinkExpired}/>
        <Route component={NotFound} />
    </Router>
    <Footer />
</div>
