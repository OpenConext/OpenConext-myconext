<script>
    import trash from "../icons/redesign/trash.svg?raw";

    export let className = "";
    export let active = false;
    export let href = "/";
    export let disabled = false;
    export let warning = false;
    export let deleteAction = false;
    export let label;
    export let onClick;
    export let medium = false;
    export let large = false;
    export let larger = false;
    export let xxl = false;
    export let small = false;
    export let download = false;
    export let deletion = false;
    export let icon = undefined;
    export let custom = false;
    export let inline = undefined;
    export let fullSize = false;

    const handleLinkClick = e => e.key === " " && e.target.click();

</script>
<style lang="scss">
    .button {
        background-color: #0077c8;
        border-radius: 8px;
        padding: 8px 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        color: white;
        text-decoration: none;
        cursor: pointer;
        text-align: center;
        font-weight: 600;
        min-width: 180px;
        text-wrap: nowrap;

        &:focus {
            background-color: #003980;
            color: #dcedff;
            outline: none;
        }
    }

    .button:hover {
        background-color: #004c97;
        color: #94d6ff;
    }

    .button.active {
        background-color: #003980;
        order: 1;
        margin-left: 20px;
    }

    .button.cancel {
        color: #0077c8;
        background-color: white;
        border: 1px solid #0066b8;
    }

    .button.cancel:hover {
        color: #0066b8;
        background-color: whitesmoke;
    }

    .button.disabled {
        cursor: not-allowed;
        color: #ababab;
        background-color: #efefef;
    }

    .button.disabled:hover {
        color: #ababab;
        background-color: #efefef;
    }

    .button.warning {
        background-color: var(--color-warning-red);

    }

    .button.warning:hover {
        background-color: #8e0000;
        color: whitesmoke;
    }

    .button.deleteAction {
        background-color: white;
        color: var(--color-warning-red);;
        border: 1px solid var(--color-warning-red);;
    }

    .button.deleteAction:hover {
        background-color: whitesmoke;
    }

    .button.small {
        min-width: 90px;
        max-width: 90px;
        padding: 6px;
    }

    .button.medium {
        min-width: 140px;
        max-width: 140px;
        width: 140px;
    }

    .button.large {
        min-width: 200px;
        max-width: 200px;
        width: 200px;
        padding: 8px 0;
    }

    .button.xxl {
        max-width: initial;
        width: initial;
        padding: 8px 0;
    }

    .button.larger {
        min-width: 240px;
        max-width: 240px;
        width: 240px;
        padding: 8px 0;
    }
    .button.full-size {
        min-width: 100%;
        max-width: 100%;
        width: 100%;
        padding: 8px 0;
    }

    .button.icon {
        padding: 10px 20px 10px 24px;
        justify-content: normal;
        &.small {
            padding: 6px 6px 6px 22px;
        }
    }

    .button.custom {
        padding-left: 42px;
        justify-content: normal;
    }

    .button.deletion {
        color: var(--color-warning-red);
        border: none;
        background-color: #f1f1f1;;
        min-width: 42px;
        width: 42px;
        height: 42px;
        min-height: 42px;
        font-size: 22px;
        padding: 8px;

        &:hover {
            color: #800101;
        }

        &.disabled {
            color: var(--color-primary-grey);
        }

    }

    .button.inline {
        color: var(--color-primary-blue);
        border: 1px solid var(--color-primary-blue);
        background-color: white;

        &:hover {
            background-color: var(--color-secondary-blue);
            color: var(--color-primary-blue);
        }
    }

    .button.ghost {
        color: black;
        border: 1px solid black;
        background-color: var(--color-secondary-blue);

        &.transparent {
            background-color: transparent;
        }
        &:hover {
            background-color: var(--color-primary-blue);
            color: white;
        }
    }

    :global(a.button span.trash svg) {
        position: absolute;
        width: 20px;
        height: auto;
        left: 11px;
        top: 8px;
    }


    :global(a.button span.icon svg) {
        position: absolute;
        width: 16px;
        height: auto;
        color: var(--color-primary-blue);
        fill: var(--color-primary-blue);
        left: 6px;
        top: 8px;
    }
    :global(a.button span.icon.custom svg) {
        position: absolute;
        width: 26px;
        height: auto;
        left: 6px;
        top: 8px;
    }

</style>

{#if download}
    <a class="{`button ${className}`}"
       href="{href}"
       download={download}
       class:medium={medium}
       class:large={large}
       class:larger={larger}
       class:xxl={xxl}
       on:click={() => onClick && onClick()}
       class:disabled={disabled}>
        {label}

    </a>
{:else}
    <a class="{`button ${className}`}"
       class:active={active}
       class:warning={warning}
       class:deleteAction={deleteAction}
       href="{href}"
       class:icon={icon != undefined}
       class:small={small}
       class:deletion={deletion}
       class:inline={inline}
       class:medium={medium}
       class:custom={custom}
       class:large={large}
       class:larger={larger}
       class:full-size={fullSize}
       class:disabled={disabled}
       on:click|preventDefault|stopPropagation={() => !disabled && onClick()}
       on:keydown={handleLinkClick}>
        {#if icon}
            <span class="icon" class:custom={custom}>{@html icon}</span>
        {/if}
        {#if deletion}
            <span class="trash">{@html trash}</span>
        {:else}
            <span>{label}</span>
        {/if}

    </a>
{/if}

