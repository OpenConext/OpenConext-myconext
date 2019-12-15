<script>
    import {user, config} from "../stores/user";
    import I18n from "i18n-js";
    import mail from "../icons/mail-white.svg";
    import phone from "../icons/phone-white.svg";
    import {Link, navigate} from "svelte-routing";
    import {onMount} from "svelte";

    import Profile from "./Profile.svelte";
    import Security from "./Security.svelte";
    import Account from "./Account.svelte";

    export let bookmark;

    const tabs = [
        {name: "profile", component: Profile, icon: mail},
        {name: "security", component: Security, icon: phone},
        {name: "account", component: Account, icon: phone}
    ];

    const links = [
        {name: "teams"}
    ];

    let currentTab = tabs[0];

    onMount(() => currentTab = bookmark ? currentTab = tabs.find(tab => tab.name === bookmark) : tabs[0]);

    const switchTab = name => () => navigate(`/${name}`);


</script>

<style>
    .home {
        height: 100%;
        display: flex;
    }

    nav {
        margin-right: 50px;
        min-width: 270px;
        background-color: #f3f6f8;
        min-height: 74vh;
    }

    @media (max-width: 720px) {
        nav {
            margin-right: 0;
        }
    }

    nav ul {
        text-align: center;
        padding: 25px 0 0 22px;
    }

    nav ul li {
        border-bottom: 1px solid #e7e7e7;
        padding: 10px 65px 10px 15px;
        text-align: left;
    }

    nav ul li:last-child {
        border-bottom: none;
    }

    nav ul li a {
        text-decoration: none;
        display: inline-block;
        margin-left: 5px;
        padding: 5px;
        font-weight: 300;
        color: black;
    }

    nav ul li a.active {
        font-weight: 400;
        color: var(--color-primary-green)
    }



</style>
    <div class="home">
        <nav>
            <ul>
                {#each tabs as tab}
                    <li>
                        {@html tab.icon}
                        <a href="/{tab.name}" class:active={tab.name === currentTab.name}
                           on:click|preventDefault|stopPropagation={switchTab(tab.name)}>
                            {I18n.ts(`home.${tab.name}`)}
                        </a>
                    </li>
                {/each}
            </ul>
        </nav>
        <svelte:component this={currentTab.component}/>
    </div>
