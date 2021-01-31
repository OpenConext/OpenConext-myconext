<script>
    export let tip = "";
</script>

<style>
    .tooltip-wrapper {
        position: relative;
        display: inline-block;
    }

    .tooltip {
        position: absolute;
        display: inline-block;
        opacity: 0;
        visibility: hidden;
        transition: opacity 150ms, visibility 150ms;
        padding: 8px 16px;
        border-radius: 6px;
        color: var(--color-primary-blue);
        background-color: #d6e9fd;

    }

    .tooltip.top {
        left: 50%;
        transform: translate(-50%, -100%);
        margin-top: -6px;
        width: 280px;
    }

    .tooltip-slot:hover + .tooltip {
        opacity: 1;
        visibility: initial;
    }

    :global(.tooltip-slot svg:hover circle, .tooltip-slot svg:hover path  ) {
        color: #0c66c4;
        fill: #0c66c4;
    }

    :global(.tooltip.top ol) {
        list-style: decimal;
        margin-left: 12px;
    }

</style>

<div class="tooltip-wrapper">
  <span class="tooltip-slot">
    <slot/>
  </span>
    <div class="tooltip top">
        {@html tip}
    </div>
</div>
