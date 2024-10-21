<script>
    import I18n from "i18n-js";
    import {startCreateFromInstitutionFlow, startLinkAccountFlow} from "../api";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";

    let fromInstitution;
    let busy = false;

    onMount(() => {
        const urlParams = new URLSearchParams(window.location.search);
        fromInstitution = urlParams.get("fromInstitution");
    });

    const retry = () => {
        busy = true;
        const promise = fromInstitution ? startCreateFromInstitutionFlow(true) : startLinkAccountFlow();
        promise.then(json => {
            window.location.href = json.url;
        });
    };

</script>

<style lang="scss">
    .attribute-missing {
        display: flex;
        flex-direction: column;
        padding: 25px;

        &.create-from-institution {
            background-color: white;
            height: auto;
            min-height: 500px;
            align-items: center;
            align-content: center;
            padding: 0
        }

        div.inner {
            margin: 25px auto auto 200px;
            max-width: 600px;

            @media (max-width: 800px) {
                margin: 25px auto;
            }
        }

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
<div class="attribute-missing" class:create-from-institution={fromInstitution}>
    <div class:inner={fromInstitution}>
        <h1>{I18n.t("attributeMissing.header")}</h1>
        <p class="last">{I18n.t(`attributeMissing.info${fromInstitution ? "New" : ""}`)}</p>
        <Button href={`/link`}
                didisabled={busy}
                large={true}
                label={I18n.t("attributeMissing.retryLink")}
                onClick={retry}/>
    </div>
</div>