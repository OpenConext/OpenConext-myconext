<script>
    import Footer from "./components/Footer.svelte";
    import {Route, Router, navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import Landing from "./routes/Landing.svelte";
    import Settings from "./routes/Settings.svelte";
    import NotFound from "./routes/NotFound.svelte";
    import Header from "./components/Header.svelte";
    import {me} from "./api";
    import {user, redirectPath} from "./stores/user";
    export let url = "";

    onMount(() => me()
            .then(json => $user = {...json, guest: false})
            .catch(() => {
                $redirectPath = window.location.pathname;
                navigate("/landing");
            }));

</script>

<style>
    .sp {
        display: flex;
        flex-direction: column;
        height: 100%;
    }
</style>
<div class="sp">
    <Header/>
    <Router url="{url}">
        <Route path="/" component={Settings}/>
        <Route path="/landing" component={Landing}/>
        <Route component={NotFound} />
    </Router>
    <Footer/>
</div>