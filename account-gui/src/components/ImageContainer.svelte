<script>

    export let icon = null;
    export let margin = true;

</script>

<style lang="scss">

    .image-container {
        display: flex;

        &.margin {
            margin: 25px 0;
        }

    }

    :global(.image-container svg,.image-container img ) {
        width: 260px;
        height: auto;
        margin: auto;
    }

</style>

<div class="image-container" class:margin>
    {#if icon}
        {@html icon}
    {:else}
        <slot/>
    {/if}
</div>