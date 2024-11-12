<script>
    import I18n from "../locale/I18n";
    import securitySvg from "../icons/redesign/lock-1.svg";
    import services from "../icons/streamline-icon-cloud-storage-drive.svg";
    import home_icon from "../icons/redesign/house.svg";
    import personalInfoSvg from "../icons/redesign/single-neutral-id-card-double.svg";
    import accountSvg from "../icons/redesign/cog.svg";
    import dataActivitySvg from "../icons/redesign/presentation-analytics.svg";
    import chevron_left from "../icons/chevron-left.svg";
    import chevron_right from "../icons/chevron-right.svg";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";

    import Start from "./Start.svelte";
    import PersonalInfo from "./PersonalInfo.svelte";
    import EditName from "./EditName.svelte";
    import EditEmail from "./EditEmail.svelte";
    import Credential from "./Credential.svelte";
    import WebAuthn from "./WebAuthn.svelte";
    import Security from "./Security.svelte";
    import DataActivity from "./DataActivity.svelte";
    import Service from "./Service.svelte";
    import Account from "./Account.svelte";
    import EppnAlreadyLinked from "./EppnAlreadyLinked.svelte";
    import DeleteAccount from "./DeleteAccount.svelte";
    import Password from "./Password.svelte";
    import Flash from "../components/Flash.svelte";
    import GetApp from "./tiqr/GetApp.svelte";
    import EnrollApp from "./tiqr/EnrollApp.svelte";
    import Recovery from "./tiqr/Recovery.svelte";
    import RecoveryCode from "./tiqr/RecoveryCode.svelte";
    import PhoneVerification from "./tiqr/PhoneVerification.svelte";
    import PhoneConfirmation from "./tiqr/PhoneConfirmation.svelte";
    import Congrats from "./tiqr/Congrats.svelte";
    import DeactivateApp from "./tiqr/DeactivateApp.svelte";
    import BackupCodes from "./BackupCodes.svelte";
    import UseApp from "./tiqr/UseApp.svelte";
    import ChangeCongrats from "./tiqr/ChangeCongrats.svelte";
    import PasswordLink from "./PasswordLink.svelte";
    import SubjectAlreadyLinked from "./SubjectAlreadyLinked.svelte";
    import ExternalAccountLinkedError from "./ExternalAccountLinkedError.svelte";
    import AttributeMissing from "./AttributeMissing.svelte";

    export let bookmark = "home";

    const tabs = [
        {name: "home", component: Start, icon: home_icon},
        {name: "personal", component: PersonalInfo, icon: personalInfoSvg},
        {name: "data-activity", component: DataActivity, icon: dataActivitySvg},
        {name: "security", component: Security, icon: securitySvg},
        {name: "account", component: Account, icon: accountSvg},

        {name: "edit-name", alias: "personal", component: EditName, ignore: true},
        {name: "manage", alias: "personal", component: PersonalInfo, ignore: true},
        {name: "edit-email", alias: "personal", component: EditEmail, ignore: true},
        {name: "eppn-already-linked", alias: "personal", component: EppnAlreadyLinked, ignore: true},
        {name: "attribute-missing", alias: "personal", component: AttributeMissing, ignore: true},
        {name: "subject-already-linked", alias: "personal", component: SubjectAlreadyLinked, ignore: true},
        {name: "external-account-linked-error", alias: "personal", component: ExternalAccountLinkedError, ignore: true},
        {name: "service", alias: "services", component: Service, ignore: true},
        {name: "credential", alias: "security", component: Credential, ignore: true},
        {name: "webauthn", alias: "security", component: WebAuthn, ignore: true},
        {name: "reset-password", alias: "security", component: Password, ignore: true},
        {name: "reset-password-link", alias: "security", component: PasswordLink, ignore: true},
        {name: "delete-account", alias: "account", component: DeleteAccount, ignore: true},

        {name: "get-app", alias: "security", component: GetApp, ignore: true},
        {name: "enroll-app", alias: "security", component: EnrollApp, ignore: true},
        {name: "recovery", alias: "security", component: Recovery, ignore: true},
        {name: "change-recovery", alias: "security", component: Recovery, ignore: true, props: {change: true}},
        {name: "recovery-code", alias: "security", component: RecoveryCode, ignore: true},
        {name: "change-recovery-code", alias: "security", component: RecoveryCode, ignore: true, props: {change: true}},
        {name: "backup-codes", alias: "security", component: BackupCodes, ignore: true},
        {name: "use-app", alias: "security", component: UseApp, ignore: true},
        {name: "phone-verification", alias: "security", component: PhoneVerification, ignore: true},
        {name: "phone-confirmation", alias: "security", component: PhoneConfirmation, ignore: true},
        {
            name: "change-phone-verification",
            alias: "security",
            component: PhoneVerification,
            ignore: true,
            props: {change: true}
        },
        {
            name: "change-phone-confirmation",
            alias: "security",
            component: PhoneConfirmation,
            ignore: true,
            props: {change: true}
        },
        {name: "congrats", alias: "security", component: Congrats, ignore: true},
        {name: "change-congrats", alias: "security", component: ChangeCongrats, ignore: true},
        {name: "deactivate-app", alias: "security", component: DeactivateApp, ignore: true},

    ];

    let currentTab = tabs[0];
    let displayMenu = false;
    let menuIcon = chevron_right;

    onMount(() => currentTab = bookmark ? currentTab = tabs.find(tab => tab.name === bookmark) : tabs[0]);

    const switchTab = name => () => {
        navigate(`/${name}`);
    }

    const showMenu = () => {
        displayMenu = !displayMenu;
        menuIcon = displayMenu ? chevron_left : chevron_right;
    }

</script>

<style lang="scss">
    .home {
        height: 100%;
        display: flex;
        position: relative;
        @media (max-width: 820px) {
            padding-right: 15px;
        }

    }

    nav {
        min-width: 246px;
        background-color: #f3f6f8;
        min-height: 100%;
        border-bottom-left-radius: 8px;
        padding-bottom: 400px;
        border-right: 2px solid var(--color-primary-grey);
    }

    nav ul {
        text-align: center;
        padding: 25px 0 0 22px;
    }

    a.menu-link {
        display: none;
    }

    @media (max-width: 820px) {
        nav {
            margin-right: 15px;
            z-index: 99;
            min-width: 50px;

        }

        nav.open {
            margin-right: 0;
            position: absolute;
            min-width: 246px;
            padding-bottom: 0;
        }

        a.menu-link {
            display: inline-block;
        }

        ul.hide {
            display: none;
        }
    }

    a.menu-link {
        padding: 10px;
    }

    nav ul li {
        display: flex;
        align-items: center;
        padding: 10px 25px 10px 15px;
    }


    nav ul li:last-child {
        border-bottom: none;
    }

    nav ul li a {
        text-decoration: none;
        display: inline-block;
        margin-left: 15px;
        font-weight: 300;
        color: black;
    }

    nav ul li {
        cursor: pointer;
        position: relative;

        &.active {
            background-color: white;
            cursor: default;

            &:after {
                content: "";
                height: 100%;
                border-right: 7px solid var(--color-primary-green);
                display: block;
                position: absolute;
                right: -5px;
            }

        }

    }

    nav ul li.active a {
        cursor: default;
    }

    nav ul li a {
        font-weight: 600;
    }

    nav ul li.active a {
        color: var(--color-primary-green);
    }

    :global(nav ul li svg) {
        width: 24px;
        height: auto;
    }

    :global(nav ul li.active svg) {
        fill: var(--color-primary-green);
        color: var(--color-primary-green);
    }

    div.component-container {
        padding: 0 50px;
        width: 100%;

        &.includes-banner {
            padding: 0;
        }
    }

    @media (max-width: 820px) {
        div.component-container {
            padding: 0;
        }
    }

</style>
<div class="home">
    <nav class:open={displayMenu}>
        <a class="menu-link" href="/menu"
           on:click|preventDefault|stopPropagation={showMenu}>{@html menuIcon}</a>
        <ul class:hide={!displayMenu}>
            {#each tabs as tab}
                {#if !tab.ignore}
                    <li class:active={tab.name === currentTab.name || tab.name === currentTab.alias}
                        on:click|preventDefault|stopPropagation={switchTab(tab.name)}>
                        {@html tab.icon}
                        <a href="/{tab.name}"
                           on:click|preventDefault|stopPropagation={switchTab(tab.name)}>
                            {I18n.t(`home.${tab.name}`)}
                        </a>
                    </li>
                {/if}
            {/each}
        </ul>
    </nav>
    <div class:includes-banner={currentTab.name === "personal" || currentTab.name === "manage"} class="component-container">
        <Flash/>
        <svelte:component this={currentTab.component} {...currentTab.props}/>
    </div>

</div>
