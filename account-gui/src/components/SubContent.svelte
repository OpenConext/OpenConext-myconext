<script>
    import {link} from "svelte-routing";

    export let question = "";
    export let preLink = "";
    export let linkText = "";
    export let route;

</script>

<style lang="scss">
    .sub-content {
        display: flex;
        flex-direction: column;
        position: relative;
        margin: 16px auto 0 auto;
        padding: 22px 32px;
        background-color: white;
        width: var(--width-app);
        justify-content: center;
        border-radius: 4px;
        box-shadow: 0 3px 0 2px #003980;
    }

    @media (max-width: 800px) {
        .sub-content {
            padding: 32px 28px;
            width: 100%;
            border-radius: 0;
            box-shadow: none;
        }
    }

    .sub-content-inner {
        display: flex;
    }

    span.question {
        font-weight: bold;
    }

    span.pre-link {
        font-weight: normal;
    }

    a {
        color: var(--color-primary-blue);
        text-decoration: underline;
        font-weight: normal;

        &:hover {
            color: var(--color-hover-blue);
            font-weight: bold;
        }
    }

</style>
<div class="sub-content">
    <div class="sub-content-inner">
        <span class="question">{@html question}
            <span class="pre-link">{preLink}</span>
            <a href={route}
               use:link>
                {linkText}
            </a>
        </span>
    </div>
</div>