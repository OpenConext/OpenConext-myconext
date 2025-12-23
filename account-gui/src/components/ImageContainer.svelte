<script>

    export let icon = null;
    export let margin = true;
    export let height = 'auto';

</script>

<style lang="scss">

    .image-container {
        display: flex;
        margin: 0 auto;

        &.margin {
            margin: 25px auto;
        }

    }

    :global(.image-container svg,.image-container img ) {
        width: 260px;
        height: var(--image-height);
        margin: auto;
    }

</style>

<div class="image-container" class:margin style="--image-height: {height}">
    {#if icon}
        {@html icon}
    {:else}
        <slot/>
    {/if}
</div>