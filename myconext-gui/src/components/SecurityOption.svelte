<script>
    import chevronRight from "../icons/chevron-right.svg?raw";
    import chevronDown from "../icons/chevron-down.svg?raw";
    import chevronUp from "../icons/chevron-up.svg?raw";

    export let icon;
    export let label;
    export let subLabel;
    export let action;
    export let active = false;
    export let hasSubContent = false;
    export let showSubContent = false;


</script>

<style lang="scss">
    .security-option {
        display: flex;
        border-radius: 6px;
        flex-direction: column;
        border: 2px solid var(--color-secondary-grey);
        padding: 16px 22px;
        cursor: pointer;
        background-color: white;

        &.active {
            border: 2px solid var(--color-tertiare-blue);
            padding: 16px 12px 16px 22px;
        }

        &:hover {
            color: var(--color-tertiare-blue);
            background-color: #c1eafe;
        }

        &:focus {
            box-shadow: 0 0 0 3px #93d5fe;
            outline: none;
            border: none;
        }

        div.security-main {
            display: flex;
            align-items: center;
        }

        span.login-icon {
            margin-right: 20px;
        }

        :global(span.login-icon svg) {
            width: 36px;
            height: auto;
        }

        .login-info p {
            font-weight: 600;
            font-size: 18px;
            color: var(--color-secondary-grey);
            &.active {
                color: var(--color-tertiare-blue);
            }

        }

        .login-info span {
            color: var(--color-tertiare-grey);
            margin-top: 6px;
            display: inline-block;
        }

        :global(span.login-icon svg) {
            color: var(--color-secondary-grey);
            fill: var(--color-secondary-grey);
        }

        :global(span.login-icon.active svg) {
            color: var(--color-tertiare-blue);
            fill: var(--color-tertiare-blue);
        }

        span.login-navigate {
            margin: 0 0 0 auto;
            display: flex;
            align-items: center;
        }

        :global(span.login-navigate svg) {
            fill: var(--color-tertiare-blue);
            width: 52px;
            height: 52px;
        }

        :global(span.login-navigate span.add) {
            color: var(--color-secondary-grey);
            font-size: 62px;
        }

    }
</style>

<div class="security-option" class:active={active} on:click={action}>
    <div class="security-main">
    <span class="login-icon" class:active={active}>{@html icon}</span>
    <div class="login-info">
        <p class:active={active}>{label}</p>
        {#if subLabel}
            <span class="sub-label">{subLabel}</span>
        {/if}
    </div>
    <span class="login-navigate"
          class:active={active}>
        {#if active && !hasSubContent}
            {@html chevronRight}
            {:else if hasSubContent && !showSubContent}
            {@html chevronDown}
            {:else if hasSubContent && showSubContent}
            {@html chevronUp}
        {:else}
            <span class="add">+</span>
        {/if}
    </span>
    </div>
    {#if hasSubContent && showSubContent}
        <slot></slot>
    {/if}
</div>

