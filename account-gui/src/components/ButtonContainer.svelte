<script>
</script>

<style lang="scss">

    .button-container {
        display: flex;
    }

    :global(.button-container a:last-child) {
        margin-left: 40px;
    }

</style>

<div class="button-container">
    <slot/>
</div>