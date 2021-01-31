<script>
    import trash from "../icons/redesign/trash.svg";

    export let className = "";
    export let active = false;
    export let href = "/";
    export let disabled = false;
    export let warning = false;
    export let label;
    export let onClick;
    export let full = false;
    export let medium = false;
    export let large = false;
    export let small = false;
    export let download = false;
    export let deletion = false;
    export let icon = undefined;

    const handleLinkClick = e => e.key === " " && e.target.click();

</script>
<style lang="scss">
    .button {
        background-color: #0077c8;
        border-radius: 8px;
        padding: 10px 20px;
        display: inline-block;
        position: relative;
        color: white;
        text-decoration: none;
        cursor: pointer;
        text-align: center;
        font-weight: bold;
        width: 216px;
    }

    @media (max-width: 820px) {
        .button {
            width: 176px;
        }

        .button.large {
            width: 260px;
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

    .button.full, .button.large {
        width: 260px;
    }

    .button.warning {
        background-color: var(--color-warning-red);

    }

    .button.warning:hover {
        background-color: #8e0000;
        color: whitesmoke;
    }

    @media (max-width: 580px) {
        .button.full {
            width: 200px;
        }

    }

    .button.small {
        max-width: 90px;
        padding: 6px;
    }

    .button.medium {
        width: 166px;
        padding: 6px;
    }

    .button.icon {
        padding: 10px 20px 10px 24px;

        &.small {
            padding: 6px 6px 6px 22px;
        }
    }

    .button.deletion {
        color: var(--color-warning-red);
        border: none;
        background-color: #f1f1f1;;
        width: 42px;
        height: 42px;
        font-size: 22px;

        &:hover {
            color: #800101;
        }

        &:disabled {
            color: var(--color-primary-grey);
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
        width: 20px;
        height: auto;
        left: 6px;
        top: 6px;
    }

</style>

{#if download}
    <a class="{`button ${className}`}"
       href="{href}"
       download={download}
       class:full={full}
       class:medium={medium}
       class:large={large}
       class:disabled={disabled}>
        {label}
    </a>
{:else}
    <a class="{`button ${className}`}"
       class:active={active}
       class:warning={warning}
       href="{href}"
       class:full={full}
       class:icon={icon != undefined}
       class:small={small}
       class:deletion={deletion}
       class:medium={medium}
       class:large={large}
       class:disabled={disabled}
       on:click|preventDefault|stopPropagation={() => !disabled && onClick()}
       on:keydown={handleLinkClick}>
        {#if icon}
            <span class="icon">{@html icon}</span>
        {/if}
        {#if deletion}
            <span class="trash">{@html trash}</span>
        {:else}
            <span>{label}</span>
        {/if}

    </a>
{/if}

