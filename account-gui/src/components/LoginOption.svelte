<script>
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";

    export let icon;
    export let translationKey;
    export let route;
    export let preferred = false;

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

        &.preferred {
            color: var(--color-primary-blue);

            &:hover {
                color: var(--color-hover-blue);
            }
        }

        span.icon {
            margin-right: 22px;
        }
    }
</style>

<div class="login-option" on:click={() => navigate(route)} class:preferred={preferred}>
    <span class="icon">{@html icon}</span>
    <span class="option">{@html I18n.t(`options.${translationKey}`)}</span>
</div>