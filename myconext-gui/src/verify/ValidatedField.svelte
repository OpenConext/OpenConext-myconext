<script>
    import shieldIcon from "../icons/redesign/shield-full.svg";

    export let label;
    export let value;
    export let icon;
    export let overrideShieldIcon;
    export let readOnly = false;

</script>
<style lang="scss">
    $max-width-not-edit: 480px;
    $max-width-mobile: 1080px;

    .validated-field {
        margin-bottom: 15px;
        max-width: $max-width-not-edit;

        @media (max-width: $max-width-mobile) {
            margin: 0 0 15px 0;
        }

    }

    .view-mode-container {
        padding: 14px;
        border: 2px solid var(--color-primary-blue);
        border-radius: 8px;
        display: flex;
        flex-direction: column;
    }

    .view-mode {
        display: flex;
        align-items: center;
        cursor: pointer;

        &.read-only {
            cursor: default;
        }

        .inner-view-mode {
            display: flex;
            flex-direction: column;
            width: 100%;

            div.value {
                color: var(--color-primary-blue);
                font-weight: 600;
                font-size: 18px;
                display: flex;
                align-items: center;

                .values {
                    display: flex;
                    flex-direction: column;
                }

                span.shield {
                    margin-right: 12px;

                    :global(svg) {
                        width: 28px;
                        height: auto;
                    }
                }

                span.preferred {
                    margin: 0 12px 0 auto;

                    :global(svg) {
                        width: 28px;
                        height: auto;
                        color: var(--color-primary-green);
                    }

                }
            }

        }

        span.editable-by {
            margin-top: 2px;
            color: var(--color-secondary-grey);
            font-size: 14px;
            font-weight: 100;
        }

    }

</style>
<div class="validated-field">
    <div class="view-mode-container">
        <div class="view-mode" class:read-only={readOnly}>
            <div class="inner-view-mode">
                <div class="value">
                    {#if overrideShieldIcon}
                        <span class="shield">{@html overrideShieldIcon}</span>
                    {:else}
                        <span class="shield">{@html shieldIcon}</span>
                    {/if}

                    <div class="values">
                        <span>{value}</span>
                        <span class="editable-by">{label}</span>
                    </div>
                    {#if icon}
                        <span class="preferred">{@html icon}</span>
                    {/if}
                </div>
            </div>
        </div>
    </div>
</div>

