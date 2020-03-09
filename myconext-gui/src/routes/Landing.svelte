<script>
    import {user, config, redirectPath} from "../stores/user";
    import {onMount} from "svelte";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";

    let isLogoutRedirect = false;
    let isAccountDeletionRedirect = false;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        isLogoutRedirect = urlSearchParams.get("logout");
        isAccountDeletionRedirect = urlSearchParams.get("delete");
        if (!isLogoutRedirect && !isAccountDeletionRedirect) {
            navigate("/404");
        }
    });

</script>

<style>
    .landing {
        display: flex;
        flex-direction: column;
        background-color: white;
        height: auto;
        min-height: 500px;
        align-items: center;
        align-content: center;
    }

    div.inner {
        margin: 25px auto;
    }

    h3 {
        color: var(--color-primary-green);
        margin-bottom: 20px;
    }

    p {
        color: var(--color-primary-black);
        font-size: 18px;
    }

</style>


<div class="landing">
    <div class="inner">
        {#if isLogoutRedirect}
            <h3>{I18n.ts("landing.logoutTitle")}</h3>
            <p>{I18n.ts("landing.logoutStatus")}</p>
        {/if}
        {#if isAccountDeletionRedirect}
            <h3>{I18n.ts("landing.deleteTitle")}</h3>
            <p>{I18n.ts("landing.deleteStatus")}</p>
        {/if}
    </div>
</div>
