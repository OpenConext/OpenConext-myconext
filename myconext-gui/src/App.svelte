<script>
    import Footer from "./components/Footer.svelte";
    import {navigate, Route, Router} from "svelte-routing";
    import {onMount} from "svelte";
    import Cookies from "js-cookie";
    import Landing from "./routes/Landing.svelte";
    import NotFound from "./routes/NotFound.svelte";
    import ConfirmUpdateEmail from "./routes/ConfirmUpdateEmail.svelte";
    import Home from "./routes/Home.svelte";
    import Header from "./components/Header.svelte";
    import {configuration, me, oidcTokens} from "./api";
    import {config, redirectPath, user} from "./stores/user";
    import I18n from "./locale/I18n";
    import CreateFromInstitution from "./routes/CreateFromInstitution.svelte";
    import Expired from "./routes/Expired.svelte";
    import EppnAlreadyLinked from "./routes/EppnAlreadyLinked.svelte";
    import LinkFromInstitution from "./routes/LinkFromInstitution.svelte";
    import AwaitLinkFromInstitutionMail from "./routes/AwaitLinkFromInstitutionMail.svelte";
    import AttributeMissing from "./routes/AttributeMissing.svelte";

    const unprotectedRoutes = [
        "/create-from-institution",
        "/landing"
    ];

    export let url = "";
    let loaded = false;

    onMount(() => configuration()
        .then(json => {
            $config = json;
            const urlSearchParams = new URLSearchParams(window.location.search);
            let lang = "en";
            if (urlSearchParams.has("lang")) {
                lang = urlSearchParams.get("lang");
            } else if (Cookies.get("lang", {domain: $config.domain})) {
                lang = Cookies.get("lang", {domain: $config.domain});
            } else {
                lang = navigator.language.toLowerCase().substring(0, 2);
            }
            if (["nl", "en"].indexOf(lang) < 0) {
                lang = "en";
            }
            I18n.changeLocale(lang);
            if (unprotectedRoutes.some(route => window.location.pathname.indexOf(route) > -1)) {
                loaded = true;
            } else {
                me()
                    .then(json => {
                        for (var key in json) {
                            if (json.hasOwnProperty(key)) {
                                $user[key] = json[key];
                            }
                        }
                        $user.guest = false;
                        const useOidcApi = $config.featureOidcTokenAPI;
                        if (useOidcApi) {
                            oidcTokens().then(tokens => {
                                $user.oidcTokens = tokens;
                                loaded = true;
                            });
                        } else {
                            loaded = true;
                        }
                    })
                    .catch(e => {
                        loaded = true;
                        $redirectPath = window.location.pathname;
                        const urlSearchParams = new URLSearchParams(window.location.search);
                        const logout = urlSearchParams.get("logout");
                        const afterDelete = urlSearchParams.get("delete");
                        if (logout) {
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
        --color-secondary-blue: #ddf2fd;
        --color-tertiare-blue: #0077c8;
        --color-primary-green: #008738;
        --color-primary-black: #202020;
        --color-primary-red: #ff0000;
        --color-warning-red: #a70000;
        --color-primary-grey: #c4cdd5;
        --color-secondary-grey: #707070;
        --color-tertiare-grey: #989898;
        --color-background: #f9f9f9;
        --color-secondary-background: #f3f6f8;
        --color-primary-yellow: #f3e178;
        --color-secondary-yellow: #fdf7d2;
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

    :global(.options a:not(:first-child)) {
        margin-left: 25px;
    }

    :global(.left) {
        background-color: #f3f6f8;
        width: 270px;
        height: 100%;
        border-bottom-left-radius: 8px;
    }

    :global(.inner) {
        margin: 20px 0 190px 0;
        padding: 15px 15px 0 40px;
        border-left: 2px solid var(--color-primary-grey);
        display: flex;
        flex-direction: column;
        background-color: white;
    }

    :global(body.modal-open) {
        overflow: hidden;
    }

    @media (max-width: 1080px) {
        .myconext {
            margin: 0;
        }

        .content {
            box-shadow: none;
        }

        :global(.options) {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
        }

        :global(.options a:not(:first-child)) {
            margin-top: 15px;
            margin-left: 0;

        }

        :global(.inner) {
            margin: 20px 0;
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
                    <Route path="/">
                        <Home bookmark="home"/>
                    </Route>
                    <Route path="/home">
                        <Home bookmark="home"/>
                    </Route>
                    <Route path="/personal">
                        <Home bookmark="personal"/>
                    </Route>
                    <Route path="/eppn-already-linked">
                        <Home bookmark="eppn-already-linked"/>
                    </Route>
                    <Route path="/attribute-missing">
                        <Home bookmark="attribute-missing"/>
                    </Route>
                    <Route path="/subject-already-linked">
                        <Home bookmark="subject-already-linked"/>
                    </Route>
                    <Route path="/external-account-linked-error">
                        <Home bookmark="external-account-linked-error"/>
                    </Route>
                    <Route path="/data-activity">
                        <Home bookmark="data-activity"/>
                    </Route>
                    <Route path="/account">
                        <Home bookmark="account"/>
                    </Route>
                    <Route path="/delete-account">
                        <Home bookmark="delete-account"/>
                    </Route>
                    <Route path="/security">
                        <Home bookmark="security"/>
                    </Route>
                    <Route path="/get-app">
                        <Home bookmark="get-app"/>
                    </Route>
                    <Route path="/enroll-app">
                        <Home bookmark="enroll-app"/>
                    </Route>
                    <Route path="/recovery">
                        <Home bookmark="recovery"/>
                    </Route>
                    <Route path="/change-recovery">
                        <Home bookmark="change-recovery"/>
                    </Route>
                    <Route path="/recovery-code">
                        <Home bookmark="recovery-code"/>
                    </Route>
                    <Route path="/change-recovery-code">
                        <Home bookmark="change-recovery-code"/>
                    </Route>
                    <Route path="/phone-verification">
                        <Home bookmark="phone-verification"/>
                    </Route>
                    <Route path="/change-phone-verification">
                        <Home bookmark="change-phone-verification"/>
                    </Route>
                    <Route path="/phone-confirmation">
                        <Home bookmark="phone-confirmation"/>
                    </Route>
                    <Route path="/change-phone-confirmation">
                        <Home bookmark="change-phone-confirmation"/>
                    </Route>
                    <Route path="/congrats">
                        <Home bookmark="congrats"/>
                    </Route>
                    <Route path="/change-congrats">
                        <Home bookmark="change-congrats"/>
                    </Route>
                    <Route path="/deactivate-app">
                        <Home bookmark="deactivate-app"/>
                    </Route>
                    <Route path="/backup-codes">
                        <Home bookmark="backup-codes"/>
                    </Route>
                    <Route path="/use-app">
                        <Home bookmark="use-app"/>
                    </Route>
                    <Route path="/services">
                        <Home bookmark="services"/>
                    </Route>
                    <Route path="/landing" component={Landing}/>
                    <Route path="/edit-name">
                        <Home bookmark="edit-name"/>
                    </Route>
                    <Route path="/edit-email">
                        <Home bookmark="edit-email"/>
                    </Route>
                    <Route path="/manage">
                        <Home bookmark="manage"/>
                    </Route>
                    <Route path="/institution">
                        <Home bookmark="institution"/>
                    </Route>
                    <Route path="/service">
                        <Home bookmark="service"/>
                    </Route>
                    <Route path="/credential">
                        <Home bookmark="credential"/>
                    </Route>
                    <Route path="/webauthn">
                        <Home bookmark="webauthn"/>
                    </Route>
                    <Route path="/reset-password-link">
                        <Home bookmark="reset-password-link"/>
                    </Route>
                    <Route path="/reset-password">
                        <Home bookmark="reset-password"/>
                    </Route>
                    <Route path="/add-password">
                        <Home bookmark="reset-password"/>
                    </Route>
                    <Route path="/update-email" component={ConfirmUpdateEmail}/>
                    <Route path="/create-from-institution" component={CreateFromInstitution}/>
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
                    {#if $config.createEduIDInstitutionEnabled}
                        <Route path="/create-from-institution" component={CreateFromInstitution}/>
                        <Route path="/create-from-Institution.Eppn.COPY-already-linked" component={EppnAlreadyLinked}/>
                        <Route path="/create-from-institution/attribute-missing" component={AttributeMissing}/>
                        <Route path="/create-from-institution/expired" component={Expired}/>
                        <Route path="/create-from-institution/poll/:hash" let:params>
                            <AwaitLinkFromInstitutionMail hash="{params.hash}"/>
                        </Route>
                        <Route path="/create-from-institution/link/:hash" let:params>
                            <LinkFromInstitution hash="{params.hash}"/>
                        </Route>
                    {/if}
                    <Route path="/landing" component={Landing}/>
                    <Route component={NotFound}/>
                </Router>
            </div>
        </div>
        <Footer/>
    </div>
{:else}
    <div class="loader"></div>
{/if}
