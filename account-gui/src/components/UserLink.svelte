<script>
    import {user} from "../stores/user";
    import {onMount} from "svelte";
    import arrowLeft from "../icons/arrow_4.svg";
    import {links} from "../stores/conf";

    $: displayUserLink = $user.knownUser &&
        ($links.userLink || window.location.href.indexOf("login") === -1 );

    let displayBackArrow = false;

    onMount(() => {
        window.addEventListener("popstate", () => {
            displayUserLink = $user.knownUser && ($links.userLink || window.location.href.indexOf("login") === -1);
        });
        displayBackArrow = window.history.length > 1;
    });

    const goBack = () => {
        window.history.back();
    }

</script>
<style lang="scss">
    div.user-link {
        display: flex;
        width: var(--width-app);
        margin: 0 auto 10px auto;
        color: white;

        span.icon {
            margin-right: 25px;
            cursor: pointer;
        }

        span.user-name {
            font-size: 14px;
        }
    }
</style>
{#if displayUserLink}
    <div class="user-link">
        {#if displayBackArrow}
            <span class="icon" on:click={goBack}>{@html arrowLeft}</span>
        {/if}
        <span class="user-name">{$user.knownUser}</span>
    </div>
{/if}

