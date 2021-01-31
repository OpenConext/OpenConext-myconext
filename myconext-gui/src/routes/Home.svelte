<script>
    import {user, config, flash} from "../stores/user";
    import I18n from "i18n-js";
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
    import DeleteAccount from "./DeleteAccount.svelte";
    import Password from "./Password.svelte";
    import Flash from "../components/Flash.svelte";

    export let bookmark = "home";

    const tabs = [
        {name: "home", component: Start, icon: home_icon},
        {name: "personal", component: PersonalInfo, icon: personalInfoSvg},
        {name: "data-activity", component: DataActivity, icon: dataActivitySvg},
        {name: "security", component: Security, icon: securitySvg},
        {name: "account", component: Account, icon: accountSvg},

        {name: "edit-name", alias: "personal", component: EditName, ignore: true},
        {name: "edit-email", alias: "personal", component: EditEmail, ignore: true},
        {name: "service", alias: "services", component: Service, ignore: true},
        {name: "credential", alias: "security", component: Credential, ignore: true},
        {name: "webauthn", alias: "security", component: WebAuthn, ignore: true},
        {name: "password", alias: "security", component: Password, ignore: true},
        {name: "forgot-password", alias: "security", component: Password, ignore: true},
        {name: "delete-account", alias: "account", component: DeleteAccount, ignore: true},

    ];

    let currentTab = tabs[0];
    let displayMenu = false;
    let menuIcon = chevron_right;

    onMount(() => currentTab = bookmark ? currentTab = tabs.find(tab => tab.name === bookmark) : tabs[0]);

    const switchTab = name => () => navigate(`/${name}`);

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
            margin-right: 20px;
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
        padding: 10px 65px 10px 15px;
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
    <div class="component-container">
        <Flash/>
        <svelte:component this={currentTab.component}/>
    </div>

</div>
