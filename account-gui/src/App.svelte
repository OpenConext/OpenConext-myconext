<script>
    import {Route, Router} from "svelte-routing";
    import Cookies from "js-cookie";
    import Login from "./routes/Login.svelte";
    import Success from "./routes/Success.svelte";
    import MagicLink from "./routes/MagicLink.svelte";
    import Confirm from "./routes/Confirm.svelte";
    import ConfirmStepup from "./routes/ConfirmStepup.svelte";
    import Migration from "./routes/Migration.svelte";
    import LinkExpired from "./routes/LinkExpired.svelte";
    import MaxAttempts from "./routes/MaxAttempts.svelte";
    import NotFound from "./routes/NotFound.svelte";
    import WebAuthn from "./routes/WebAuthn.svelte";
    import WebAuthnTest from "./routes/WebAuthnTest.svelte";
    import Header from "./components/Header.svelte";
    import Footer from "./components/Footer.svelte";
    import {onMount} from "svelte";
    import {configuration} from "./api";
    import I18n from "i18n-js";
    import {conf} from "./stores/conf";
    import Loader from "./components/Loader.svelte";
    import Stepup from "./routes/Stepup.svelte";
    import AffiliationMissing from "./routes/AffiliationMissing.svelte";
    import ValidNameMissing from "./routes/ValidNameMissing.svelte";
    import EppnAlreadyLinked from "./routes/EppnAlreadyLinked.svelte";

    export let url = "";

    let loaded = false;

    onMount(() => configuration()
        .then(json => {
            $conf = json;
            if (typeof window !== "undefined") {
                const urlSearchParams = new URLSearchParams(window.location.search);
                if (urlSearchParams.has("lang")) {
                    I18n.locale = urlSearchParams.get("lang").toLowerCase();
                } else if (Cookies.get("lang", {domain: $conf.domain})) {
                    I18n.locale = Cookies.get("lang", {domain: $conf.domain}).toLowerCase();
                } else {
                    I18n.locale = navigator.language.toLowerCase().substring(0, 2);
                }
            } else {
                I18n.locale = "en";
            }
            if (["nl", "en"].indexOf(I18n.locale) < 0) {
                I18n.locale = "en";
            }
            loaded = true;
        }));

</script>

<style>

    :global(:root){
        --color-primary-blue: #0062b0;
        --color-hover-blue: #003980;
        --color-primary-green: #008738;
        --color-primary-black: #202020;
        --color-primary-red: #ff0000;
        --color-primary-grey: #d0d0d0;

        --width-app: 400px;

    }

    :global(input::placeholder) {
        color: #8b8b8b;
        font-style: italic;
    }

    .idp {
        display: flex;
        flex-direction: column;
        margin-bottom: 100px;
    }
    .content {
        display: flex;
        flex-direction: column;
        position: relative;
        padding: 24px 33px 40px;
        background-color: white;
        width: var(--width-app);
        margin: 0 auto;
        justify-content: center;
        border-radius: 4px;
        box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.5);
    }

    @media (max-width: 800px) {
        .idp {
            margin: 0;
        }
        .content {
            padding: 32px 28px;
            width: 100%;
            border-radius: 0;
            box-shadow: none;
        }
    }

</style>
{#if loaded}
    <div class="idp">
        <Header/>
        <div class="content">
            <Router url="{url}">
                <Route path="/login/:id" let:params>
                    <Login id="{params.id}"/>
                </Route>
                <Route path="/magic/:id" let:params>
                    <MagicLink id="{params.id}"/>
                </Route>
                <Route path="/stepup/:id" let:params>
                    <Stepup id="{params.id}"/>
                </Route>
                <Route path="/affiliation-missing/:id" let:params>
                    <AffiliationMissing id="{params.id}"/>
                </Route>
                <Route path="/valid-name-missing/:id" let:params>
                    <ValidNameMissing id="{params.id}"/>
                </Route>
                <Route path="/eppn-already-linked/:id" let:params>
                    <EppnAlreadyLinked id="{params.id}"/>
                </Route>
                <Route path="/confirm" component={Confirm}/>
                <Route path="/confirm-stepup" component={ConfirmStepup}/>
                <Route path="/migration" component={Migration}/>
                <Route path="/success" component={Success}/>
                <Route path="/expired" component={LinkExpired}/>
                <Route path="/max-attempts" component={MaxAttempts}/>
                <Route path="/webauthn" component={WebAuthn}/>
                <Route path="/webauthnTest/:id" let:params>
                    <WebAuthnTest id="{params.id}"></WebAuthnTest>
                </Route>
                <Route component={NotFound}/>
            </Router>
        </div>
        <Footer/>
    </div>
{:else}
    <Loader/>
{/if}
