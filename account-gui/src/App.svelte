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
    import {onMount} from "svelte";
    import {configuration} from "./api";
    import I18n from "i18n-js";

    export let url = "";

    let loaded = false;

    onMount(() => configuration()
            .then(json => {
                I18n.branding = json.branding;
                loaded = true;
            }));

</script>

<style>

    :global(:root){
        --color-primary-blue: #0061b0;
        --color-primary-green: #008738;
        --color-primary-black: #202020;
        --color-primary-red: #d00000;

        --width-app: 502px;
    }

    .idp {
        display: flex;
        flex-direction: column;
        margin-bottom: 100px;
    }
    .content {
        display: flex;
        justify-content: center;
        border-left: 2px solid var(--color-primary-blue);
        border-right: 2px solid var(--color-primary-blue);
        flex-direction: column;
        padding: 24px 33px 40px;
        background-color: white;
        width: var(--width-app);
        margin: 0 auto;
        border-left: 2px solid var(--color-primary-blue);
        border-bottom-left-radius: 10px;
        border-bottom-right-radius: 10px;
        border-right: 2px solid var(--color-primary-blue);
        border-bottom: 4px solid var(--color-primary-blue);
    }

    @media (max-width: 600px) {
        .idp {
            margin: 0 15px;
        }
        .content {
            padding: 32px 28px;
            width: 100%;
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
        border: 1.1em solid #4DB2CF;
        border-left-color: white;
        -webkit-transform: translateZ(0);
        -ms-transform: translateZ(0);
        transform: translateZ(0);
        -webkit-animation: load8 1.1s infinite linear;
        animation: load8 1.1s infinite linear;
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
