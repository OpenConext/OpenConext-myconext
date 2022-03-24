<script>
    import {user} from "../stores/user";
    import {onMount} from "svelte";
    import arrowLeft from "../icons/arrow_4.svg";

    let displayUserLink = false;

    onMount(() => {
        displayUserLink = $user.knownUser && window.location.href.indexOf("login") === -1;
        window.addEventListener("popstate", () => {
            displayUserLink = $user.knownUser && document.location.href.indexOf("login") === -1;
        });
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
        <span class="icon" on:click={goBack}>{@html arrowLeft}</span>
        <span class="user-name">{$user.knownUser}</span>
    </div>
{/if}

