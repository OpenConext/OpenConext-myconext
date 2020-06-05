<script>
    import Footer from "./components/Footer.svelte";
    import {Route, Router, navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import Landing from "./routes/Landing.svelte";
    import NotFound from "./routes/NotFound.svelte";
    import EditName from "./routes/EditName.svelte";
    import Institution from "./routes/Institution.svelte";
    import MigrationError from "./routes/MigrationError.svelte";
    import Password from "./routes/Password.svelte";
    import WebAuthn from "./routes/WebAuthn.svelte";
    import RememberMe from "./routes/RememberMe.svelte";
    import Home from "./routes/Home.svelte";
    import Header from "./components/Header.svelte";
    import {me, configuration} from "./api";
    import {user, config, redirectPath, duplicatedEmail} from "./stores/user";
    import I18n from "i18n-js";

    export let url = "";
    let loaded = false;

    onMount(() => configuration()
            .then(json => {
                $config = json;
                if (typeof window !== "undefined") {
                    const urlSearchParams = new URLSearchParams(window.location.search);
                    if (urlSearchParams.has("lang")) {
                        I18n.locale = urlSearchParams.get("lang");
                    } else if (Cookies.get("lang", {domain: $config.domain})) {
                        I18n.locale = Cookies.get("lang", {domain: $config.domain});
                    } else {
                        I18n.locale = navigator.language.toLowerCase().substring(0, 2);
                    }
                } else {
                    I18n.locale = "en";
                }
                if (["nl", "en"].indexOf(I18n.locale) < 0) {
                    I18n.locale = "en";
                }
                if (window.location.pathname.indexOf("landing") > -1) {
                    loaded = true;
                } else {
                    me()
                            .then(json => {
                                loaded = true;
                                for (var key in json) {
                                    if (json.hasOwnProperty(key)) {
                                        $user[key] = json[key];
                                    }
                                }
                                $user.guest = false;
                            })
                            .catch(e => {
                                loaded = true;
                                $redirectPath = window.location.pathname;
                                const urlSearchParams = new URLSearchParams(window.location.search);
                                const logout = urlSearchParams.get("logout");
                                const afterDelete = urlSearchParams.get("delete");
                                if (e.status === 409) {
                                    e.json().then(res => {
                                        $duplicatedEmail = res.email;
                                        navigate("/migration-error");
                                    });
                                } else if (logout) {
                                    navigate("/landing?logout=true");
                                } else if (afterDelete) {
                                    navigate("/landing?delete=true");
                                } else {
                                    const path = encodeURIComponent($redirectPath || "/");
                                    window.location.href = `${$config.loginUrl}?redirect_path=${path}`;
                                }
                            })
                }

            })
    );

</script>

<style>

    :global(:root) {
        --color-primary-blue: #0062b0;
        --color-primary-green: #008738;
        --color-primary-black: #202020;
        --color-primary-red: #ff0000;
        --color-primary-grey: #c4cdd5;
        --color-background: #f9f9f9;
        --width-app: 1024px;
    }

    .myconext {
        display: flex;
        flex-direction: column;
        height: 100%;
        margin-bottom: 100px;
    }

    .container {
        max-width: var(--width-app);
        margin: 0 auto;
        width: 100%;
        display: flex;
        flex-direction: column;
        box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.5);
    }

    .content {
        display: flex;
        flex-direction: column;
        background-color: white;
        align-items: stretch;
        max-width: var(--width-app);
        width: 100%;
        margin: 0 auto;
    }

    @media (max-width: 1250px) {
        .myconext {
            margin: 0 15px;
        }

        .content {
            width: 100%;
        }
    }

    @media (max-width: 800px) {
        .myconext {
            margin: 0;
        }

        .content {
            box-shadow: none;
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
{#if loaded && !$user.guest}
    <div class="myconext">
        <div class="container">
            <Header/>
            <div class="content">
                <Router url="{url}">
                    <Route path="/" component={Home}/>
                    <Route path="/profile">
                        <Home bookmark="profile"/>
                    </Route>
                    <Route path="/account">
                        <Home bookmark="account"/>
                    </Route>
                    <Route path="/institutions">
                        <Home bookmark="institutions"/>
                    </Route>
                    <Route path="/security">
                        <Home bookmark="security"/>
                    </Route>
                    <Route path="/landing" component={Landing}/>
                    <Route path="/edit" component={EditName}/>
                    <Route path="/institution" component={Institution}/>
                    <Route path="/migration">
                        <Home bookmark="migration"/>
                    </Route>
                    <Route path="/password" component={Password}/>
                    <Route path="/webauthn" component={WebAuthn}/>
                    <Route path="/rememberme" component={RememberMe}/>
                    <Route component={NotFound}/>
                </Router>
            </div>
        </div>
        <Footer/>
    </div>
{:else if loaded && $user.guest}
    <div class="myconext">
        <div class="container">
            <Header/>
            <div class="content">
                <Router url="{url}">
                    <Route path="/" component={Home}/>
                    <Route path="/landing" component={Landing}/>
                    <Route path="/migration-error" component={MigrationError}/>
                    <Route component={NotFound}/>
                </Router>
            </div>
        </div>
        <Footer/>
    </div>
{:else}
    <div class="loader"></div>
{/if}
