<script>
    import {Route, Router} from "svelte-routing";
    import Cookies from "js-cookie";
    import Login from "./routes/Login.svelte";
    import Success from "./routes/Success.svelte";
    import LoginCode from "./routes/LoginCode.svelte";
    import Confirm from "./routes/Confirm.svelte";
    import ConfirmStepup from "./routes/ConfirmStepup.svelte";
    import LinkExpired from "./routes/LinkExpired.svelte";
    import MaxAttempts from "./routes/MaxAttempts.svelte";
    import NotFound from "./routes/NotFound.svelte";
    import WebAuthn from "./routes/WebAuthn.svelte";
    import WebAuthnTest from "./routes/WebAuthnTest.svelte";
    import Header from "./components/Header.svelte";
    import PhoneConfirmation from "./routes/PhoneConfirmation.svelte";
    import PhoneVerification from "./routes/PhoneVerification.svelte";
    import Footer from "./components/Footer.svelte";
    import {onMount} from "svelte";
    import {allowedEmailDomains, configuration, institutionalEmailDomains} from "./api";
    import I18n from "./locale/I18n";
    import {conf} from "./stores/conf";
    import {domains} from "./stores/domains";
    import Loader from "./components/Loader.svelte";
    import Stepup from "./routes/Stepup.svelte";
    import AffiliationMissing from "./routes/AffiliationMissing.svelte";
    import ValidNameMissing from "./routes/ValidNameMissing.svelte";
    import EppnAlreadyLinked from "./routes/EppnAlreadyLinked.svelte";
    import Request from "./routes/Request.svelte";
    import SubContent from "./components/SubContent.svelte";
    import {user} from "./stores/user";
    import {cookieNames} from "./constants/cookieNames";
    import UseApp from "./routes/UseApp.svelte";
    import UseWebAuth from "./routes/UseWebAuth.svelte";
    import UseCode from "./routes/UseCode.svelte";
    import UsePassword from "./routes/UsePassword.svelte";
    import Options from "./routes/Options.svelte";
    import UserLink from "./components/UserLink.svelte";
    import GetApp from "./routes/GetApp.svelte";
    import EnrollApp from "./routes/EnrollApp.svelte";
    import Recovery from "./routes/Recovery.svelte";
    import RecoveryCode from "./routes/RecoveryCode.svelte";
    import Congrats from "./routes/Congrats.svelte";
    import RememberMe from "./routes/RememberMe.svelte";
    import AppRequired from "./routes/AppRequired.svelte";
    import RedirectMobileApp from "./routes/RedirectMobileApp.svelte";
    import ConfirmExternalStepup from "./routes/ConfirmExternalStepup.svelte";
    import SubjectAlreadyLinked from "./routes/SubjectAlreadyLinked.svelte";
    import ExternalAccountLinkedError from "./routes/ExternalAccountLinkedError.svelte";
    import AttributeMissing from "./routes/AttributeMissing.svelte";
    import RateLimited from "./routes/RateLimited.svelte";
    import InstallApp from "./routes/InstallApp.svelte";

    export let url = "";

    let loaded = false;

    onMount(() => configuration()
        .then(json => {
            $conf = json;
            const urlSearchParams = new URLSearchParams(window.location.search);
            let locale = "en";
            if (urlSearchParams.has("lang")) {
                locale = urlSearchParams.get("lang").toLowerCase();
            } else if (Cookies.get("lang", {domain: $conf.domain})) {
                locale = Cookies.get("lang", {domain: $conf.domain}).toLowerCase();
            } else {
                locale = navigator.language.toLowerCase().substring(0, 2);
            }
            if (["nl", "en"].indexOf(locale) < 0) {
                locale = "en";
            }
            I18n.changeLocale(locale);

            $user.knownUser = Cookies.get(cookieNames.USERNAME);
            $user.email = $user.knownUser || "";
            $user.preferredLogin = Cookies.get(cookieNames.LOGIN_PREFERENCE);

            loaded = true;

            if ($conf.featureWarningEducationalEmailDomain) {
                institutionalEmailDomains().then(json => {
                    $domains.institutionDomainNames = json;
                });
            }
            if ($conf.featureAllowList) {
                allowedEmailDomains().then(json => {
                    $domains.allowedDomainNames = json;
                })
            }
        }));

</script>

<style>

    :global(:root) {
        --color-primary-blue: #0062b0;
        --color-hover-blue: #003980;
        --color-primary-green: #008738;
        --color-primary-black: #202020;
        --color-primary-red: #ff0000;
        --color-primary-grey: #d0d0d0;
        --color-secondary-grey: #707070;
        --color-tertiare-grey: #989898;
        --color-background: #eaeaea;
        --width-app: 400px;

    }

    :global(input::placeholder) {
        color: #8b8b8b;
        font-style: italic;
    }

    :global(strong) {
        font-weight: 600;
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
        padding: 30px 32px 32px;
        background-color: white;
        width: var(--width-app);
        margin: 0 auto;
        justify-content: center;
        border-radius: 4px;
        box-shadow: 0 3px 0 2px #003980;
        min-height: 100px;
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
        <UserLink/>
        <div class="content">
            <Router url="{url}">
                <Route path="/login/:id" let:params>
                    <Login id="{params.id}"/>
                </Route>
                <Route path="/request/:id" let:params>
                    <Request id="{params.id}"/>
                </Route>
                <Route path="/useapp/:id" let:params>
                    <UseApp id="{params.id}"/>
                </Route>
                <Route path="/usewebauthn/:id" let:params>
                    <UseWebAuth id="{params.id}"/>
                </Route>
                <Route path="/usecode/:id" let:params>
                    <UseCode id="{params.id}"/>
                </Route>
                <Route path="/app-required" component={AppRequired}/>
                <Route path="/getapp" component={GetApp}/>
                <Route path="/install-app" component={InstallApp}/>
                <Route path="/enrollapp" component={EnrollApp}/>
                <Route path="/recovery" component={Recovery}/>
                <Route path="/recovery-code" component={RecoveryCode}/>
                <Route path="/phone-verification" component={PhoneVerification}/>
                <Route path="/phone-confirmation" component={PhoneConfirmation}/>
                <Route path="/congrats" component={Congrats}/>
                <Route path="/remember" component={RememberMe}/>
                <Route path="/rate-limited" component={RateLimited}/>
                <Route path="/usepassword/:id" let:params>
                    <UsePassword id="{params.id}"/>
                </Route>
                <Route path="/code/:id" let:params>
                    <LoginCode id="{params.id}"/>
                </Route>
                <Route path="/options/:id" let:params>
                    <Options id="{params.id}"/>
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
                <Route path="/attribute-missing/:id" let:params>
                    <AttributeMissing id="{params.id}"/>
                </Route>
                <Route path="/subject-already-linked/:id" let:params>
                    <SubjectAlreadyLinked id="{params.id}"/>
                </Route>
                <Route path="/external-account-linked-error" component={ExternalAccountLinkedError}/>
                <Route path="/confirm" component={Confirm}/>
                <Route path="/confirm-stepup" component={ConfirmStepup}/>
                <Route path="/confirm-external-stepup" component={ConfirmExternalStepup}/>
                <Route path="/success" component={Success}/>
                <Route path="/expired" component={LinkExpired}/>
                <Route path="/max-attempts" component={MaxAttempts}/>
                <Route path="/webauthn" component={WebAuthn}/>
                <Route path="/webauthnTest/:id" let:params>
                    <WebAuthnTest id="{params.id}"/>
                </Route>
                <Route path="/client/mobile/*action" component={RedirectMobileApp}/>
                <Route component={NotFound}/>
            </Router>
        </div>
        <Router url="{url}">
            <Route path="/login/:id" let:params>
                <SubContent question={I18n.t("Login.RequestEduId.COPY")}
                            linkText={I18n.t("Login.RequestEduId2.COPY")}
                            route="/request/{params.id}"/>
            </Route>
            <Route path="/request/:id" let:params>
                <SubContent question={I18n.t("Login.AlreadyGuestAccount.COPY")}
                            linkText={I18n.t("Login.LoginEduId.COPY")}
                            route="/login/{params.id}"/>
            </Route>
            <Route path="/useapp/:id" let:params>
                <SubContent question={I18n.t("Login.NoAppAccess.COPY")}
                            preLink={I18n.t("Login.UseAnother.COPY")}
                            isMfa={true}
                            linkText={I18n.t("Login.OptionsLink.COPY")}
                            route="/options/{params.id}"/>
            </Route>
            <Route path="/usewebauthn/:id" let:params>
                <SubContent question={I18n.t("Login.UseAnother.COPY")}
                            linkText={I18n.t("Login.OptionsLink.COPY")}
                            route="/options/{params.id}"/>
            </Route>
            <Route path="/usecode/:id" let:params>
                <SubContent question={I18n.t("Login.NoMailAccess.COPY")}
                            preLink={I18n.t("Login.UseAnother.COPY")}
                            linkText={I18n.t("Login.OptionsLink.COPY")}
                            route="/options/{params.id}"/>
            </Route>
            <Route path="/code/:id" let:params>
                <SubContent question={I18n.t("Login.NoMailAccess.COPY")}
                            preLink={I18n.t("Login.UseAnother.COPY")}
                            linkText={I18n.t("Login.OptionsLink.COPY")}
                            route="/options/{params.id}"/>
            </Route>
            <Route path="/usepassword/:id" let:params>
                <SubContent question={I18n.t("Login.ForgotPassword.COPY")}
                            preLink={I18n.t("Login.UseAnother.COPY")}
                            linkText={I18n.t("Login.OptionsLink.COPY")}
                            route="/options/{params.id}"/>
            </Route>
            <Route path="/options/:id" let:params>
                <SubContent question={I18n.t("Options.NoLogin.COPY")}
                            preLink={I18n.t("Options.Learn.COPY")}
                            linkText={I18n.t("Options.LearnLink.COPY")}
                            href="https://eduid.nl/help"/>
            </Route>
        </Router>
        <Footer/>
    </div>
{:else}
    <Loader/>
{/if}
