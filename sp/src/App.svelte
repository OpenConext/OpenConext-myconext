<script>
    import Footer from "./components/Footer.svelte";
    import {Route, Router} from "svelte-routing";
    import Landing from "./routes/Landing.svelte";
    import Header from "./components/Header.svelte";

    export let url = "";
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
        <Route path="/" component={Landing}/>
    </Router>
    <Footer/>
</div>