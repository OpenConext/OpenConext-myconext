<script>
    import {user} from "../stores/user";
    import I18n from "i18n-js";
    import {Link, navigate} from "svelte-routing";
    import {onMount} from "svelte";

    import Profile from "./Profile.svelte";
    import Security from "./Security.svelte";

    export let bookmark;

    const tabs = [
        {name: "profile", component: Profile},
        {name: "security", component: Security}
    ];

    let currentTab = tabs[0];

    onMount(() => currentTab = bookmark ? currentTab = tabs.find(tab => tab.name === bookmark) : tabs[0]);

    const switchTab = name => () => navigate(`/${name}`);


</script>

<style>
    .home {
    }

    .inner {
        max-width: 1080px;
        margin: 0 auto;
        padding: 60px 0 0 0;
        font-size: 20px;
        display: flex;
    }

    nav {
        margin-right: 50px;
    }

</style>
<div class="home">
    <div class="inner">
        <nav>
            <ul>
                {#each tabs as tab}
                    <li>
                        <a href="/{tab.name}" on:click|preventDefault|stopPropagation={switchTab(tab.name)}>
                            {I18n.t(`home.${tab.name}`)}
                        </a>
                    </li>
                {/each}
            </ul>
        </nav>
        <svelte:component this={currentTab.component}/>
    </div>

</div>