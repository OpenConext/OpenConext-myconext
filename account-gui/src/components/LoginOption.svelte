<script>
    import I18n from "../locale/I18n";
    import {navigate} from "svelte-routing";
    import DOMPurify from "dompurify";

    export let icon;
    export let label;
    export let subLabel;
    export let translationKey;
    export let route;
    export let action;
    export let index;
    export let preferred = false;

    const init = el => preferred && el.focus();

    const handleKeyDown = route => e => {
        if (e.key === "Enter" || e.code === "Space") {
            e.stopPropagation();
            e.preventDefault();
            route ? navigate(route) : action();
        }
    };

</script>

<style lang="scss">
    .login-option {
        display: flex;
        border-radius: 6px;
        box-shadow: 0 1px 0 2px #5e6873;
        align-items: center;
        padding: 16px 22px;
        margin-bottom: 20px;
        cursor: pointer;

        &:hover {
            color: var(--color-primary-blue);
            background-color: #c1eafe;
        }

        &:focus {
            box-shadow: 0 0 0 3px #93d5fe;
            outline: none;
            border: none;
        }

        span.option-container {
            display: flex;
            flex-direction: column
        }

        span.has-sub-label {
            font-weight: 600;
        }

        span.login-icon {
            margin-right: 20px;
        }

        :global(span.login-icon svg) {
            width: 32px;
            height: 32px;
        }
    }
</style>

<div class="login-option" use:init
     on:click={() => route ? navigate(route) : action()}
     on:keydown={handleKeyDown(route)}
     tabindex={index}>
    <span class="login-icon">{@html icon}</span>
    <span class="option-container">
        <span class:has-sub-label={subLabel}>{@html DOMPurify.sanitize(translationKey ? I18n.t(`options.${translationKey}`) : label)}</span>
        {#if subLabel}
        <span class="sub-label">{subLabel}</span>
    {/if}
        </span>
</div>