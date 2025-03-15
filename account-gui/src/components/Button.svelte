<script>
    export let className = "";
    export let active = false;
    export let href = "/";
    export let disabled = false;
    export let label;
    export let onClick;

    const handleLinkClick = e => e.key === " " && e.target.click();

</script>
<style lang="scss">
    .button {
        width: 100%;
        background-color: #0077c8;
        border-radius: 8px;
        padding: 10px 20px;
        display: inline-block;
        color: white;
        text-decoration: none;
        cursor: pointer;
        text-align: center;
        font-weight: 600;
        font-size: 18px;
        text-wrap: nowrap;
        white-space: nowrap;

        &:hover {
            background-color: #004c97;
            color: #94d6ff;
        }

        &:focus {
            background-color: #003980;
            color: #dcedff;
            outline: none;
        }
    }

    .button.active {
        background-color: #003980;
        order: 1;
        margin-left: 20px;
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

    .button.cancel {
        color: #0077c8;
        background-color: white;
        border: 1px solid #0066b8;
    }

    .button.cancel:hover {
        color: #0066b8;
        background-color: whitesmoke;
    }

    .button.full {
        margin-top: 15px;
        margin-left: 0;
    }

</style>

<a class="{`button ${className}`}"
   class:active={active}
   href="{href}"
   class:disabled={disabled}
   on:click|preventDefault|stopPropagation={() => !disabled && onClick()}
   on:keydown={handleLinkClick}>
    {label}
</a>