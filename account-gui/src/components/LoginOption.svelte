<script>
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";

    export let icon;
    export let translationKey;
    export let route;
    export let index;
    export let preferred = false;

    const init = el => preferred && el.focus();

    const handleKeyDown = route => e => {
        if (e.key === "Enter" || e.code === "Space") {
            e.stopPropagation();
            e.preventDefault();
            navigate(route);
        }
    };

</script>

<style lang="scss">
    .login-option {
        display: flex;
        align-items: center;
        border-radius: 6px;
        border: 1px solid var(--color-primary-grey);
        padding: 16px 22px;
        margin-bottom: 20px;
        cursor: pointer;

        &:hover {
            color: var(--color-primary-blue);
            background-color: #fafafa;
        }

        &:focus {
            box-shadow: 0 0 0 3px #94d6ff;
            outline: none;
            border: none;
        }

        span.icon {
            margin-right: 22px;
        }
    }
</style>

<div class="login-option" use:init on:click={() => navigate(route)} on:keydown={handleKeyDown(route)} tabindex={index}>
    <span class="icon">{@html icon}</span>
    <span class="option">{@html I18n.t(`options.${translationKey}`)}</span>
</div>