<script>
    import I18n from "i18n-js";
    import {startLinkAccountFlow} from "../api";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";

    let email;

    onMount(() => {
        const urlParams = new URLSearchParams(window.location.search);
        email = decodeURIComponent(urlParams.get("email"));
    });

    const retry = () => startLinkAccountFlow().then(json => {
        window.location.href = json.url;
    });

</script>

<style lang="scss">
    .eppn-already-linked {
        display: flex;
        flex-direction: column;
        padding: 25px;

        h1 {
            color: var(--color-primary-red);
            font-size: 32px;
            font-weight: bold;
            margin: 22px 0;
        }

        p {
            margin-bottom: 12px;

            &.last {
                margin-bottom: 40px;
            }
        }

    }


</style>
<div class="eppn-already-linked">
    <h1>{I18n.t("eppnAlreadyLinked.header")}</h1>
    <p class="last">{I18n.t("eppnAlreadyLinked.info", {email: email})}</p>
    <Button href={`/link`} label={I18n.t("eppnAlreadyLinked.retryLink")} onClick={retry}/>
</div>