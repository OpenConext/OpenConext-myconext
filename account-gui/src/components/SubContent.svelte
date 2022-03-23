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
    }

    span.question {
        font-weight: bold;
    }

    a:hover {
        color: var(--color-hover-blue);
        font-weight: bold;
    }

    a {
        color: var(--color-primary-blue);
        text-decoration: underline;
        font-weight: normal;
    }

</style>
<div class="sub-content">
        <span class="question">{@html question}
            <span class="pre-link">{preLink}</span>
            <a class="toggle-link"
               href={route}
               use:link>
                {linkText}
            </a>
        </span>
</div>