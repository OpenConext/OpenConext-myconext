<script>
    import {user} from "../stores/user";
    import {onMount} from "svelte";
    import arrowLeft from "../icons/arrow_4.svg";
    import {links} from "../stores/conf";

    $: displayUserLink = $user.knownUser && $user.knownUser.length > 0 && $links.userLink;
    $: displayBackArrow = $user.knownUser && $links.displayBackArrow && window.history.length > 1;

    onMount(() => {
        window.addEventListener("popstate", e => {
            displayUserLink = $user.knownUser && $links.userLink && window.location.href.indexOf("login") === -1;
            displayBackArrow = $user.knownUser && $links.displayBackArrow && window.history.length > 1;
        });
    });

    const goBack = () => {
        window.history.back();
    }

</script>
<style lang="scss">
    div.user-link {
        display: flex;
        margin: 0 auto 6px auto;
        color: white;
        width: var(--width-app);

        span.icon {
            margin-right: 15px;
            cursor: pointer;
        }

    }

    @media (max-width: 800px) {
        div.user-link {
            margin: 0 auto 6px 10px;
            width: 90%;
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

