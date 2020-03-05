<script>
    import {Route, Router} from "svelte-routing";
    import Login from "./routes/Login.svelte";
    import SessionLost from "./routes/SessionLost.svelte";
    import MagicLink from "./routes/MagicLink.svelte";
    import Confirm from "./routes/Confirm.svelte";
    import Migration from "./routes/Migration.svelte";
    import LinkExpired from "./routes/LinkExpired.svelte";
    import NotFound from "./routes/NotFound.svelte";
    import Header from "./components/Header.svelte";
    import Footer from "./components/Footer.svelte";
    import {onMount} from "svelte";
    import {configuration} from "./api";
    import I18n from "i18n-js";
    import {conf} from "./stores/conf";

    export let url = "";

    let loaded = false;

    onMount(() => configuration()
            .then(json => {
                I18n.branding = json.branding;
                $conf = json;
                loaded = true;
            }));

</script>

<style>

    :global(:root){
        --color-primary-blue: #0062b0;
        --color-primary-green: #008738;
        --color-primary-black: #202020;
        --color-primary-red: #ff0000;

        --width-app: 400px;
    }

    .idp {
        display: flex;
        flex-direction: column;
        margin-bottom: 100px;
    }
    .content {
        display: flex;
        flex-direction: column;
        padding: 24px 33px 40px;
        background-color: white;
        width: var(--width-app);
        margin: 0 auto;
        justify-content: center;
        border-radius: 4px;
        box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.5);
    }

    @media (max-width: 600px) {
        .idp {
            margin: 0;
        }
        .content {
            padding: 32px 28px;
            width: 100%;
            border-radius: 0;
        }
    }

    .loader:empty,
    .loader:empty:after {
        border-radius: 50%;
        width: 10em;
        height: 10em;
    }

    .loader:empty {
        margin: 60px auto;
        font-size: 10px;
        position: relative;
        text-indent: -9999em;
        border: 1.1em solid white;
        border-top-color: var(--color-primary-green);
        border-bottom-color: var(--color-primary-blue);
        -webkit-transform: translateZ(0);
        -ms-transform: translateZ(0);
        transform: translateZ(0);
        -webkit-animation: load8 1.5s infinite linear;
        animation: load8 1.5s infinite linear;
    }

    @-webkit-keyframes load8 {
        0% {
            -webkit-transform: rotate(0deg);
            transform: rotate(0deg);
        }
        100% {
            -webkit-transform: rotate(360deg);
            transform: rotate(360deg);
        }
    }

    @keyframes load8 {
        0% {
            -webkit-transform: rotate(0deg);
            transform: rotate(0deg);
        }
        100% {
            -webkit-transform: rotate(360deg);
            transform: rotate(360deg);
        }
    }
</style>
{#if loaded}
    <div class="idp">
        <Header/>
        <div class="content">
            <Router url="{url}">
                <Route path="/login/:id" let:params>
                    <Login id="{params.id}"></Login>
                </Route>
                <Route path="/magic/:id" let:params>
                    <MagicLink id="{params.id}"></MagicLink>
                </Route>
                <Route path="/confirm" component={Confirm}/>
                <Route path="/migration" component={Migration}/>
                <Route path="/session" component={SessionLost}/>
                <Route path="/expired" component={LinkExpired}/>
                <Route component={NotFound}/>
            </Router>
        </div>
        <Footer/>
    </div>
{:else}
    <div class="loader"></div>
{/if}
