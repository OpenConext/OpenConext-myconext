<script>
    import {config} from "../stores/user";
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";
    import {startCreateFromInstitutionFlow} from "../api";
    import {onMount} from "svelte";
    import Spinner from "../components/Spinner.svelte";

    let busy = false;

    onMount(() => {
        if (!$config.createEduIDInstitutionLanding) {
            startFlow();
        }
    })

    const startFlow = () => {
        busy = true;
        startCreateFromInstitutionFlow().then(res => {
            window.location.href = res.url;
        })

    }

</script>

<style lang="scss">
    .create-from-institution {
        display: flex;
        flex-direction: column;
        background-color: white;
        height: auto;
        min-height: 500px;
        align-items: center;
        align-content: center;
    }

    div.inner {
        margin: 25px auto;
        max-width: 600px;
    }

    h3 {
        color: var(--color-primary-green);
        margin-bottom: 40px;
    }

    div.info {
        p {
            margin-top: 10px;
        }

        p.header {
            margin-top: 0;
            margin-bottom: 25px;
        }
    }

    div.actions {
        margin-top: 25px;
    }

    div.footer {
        margin-top: 25px;
    }

</style>


<div class="create-from-institution">
    {#if $config.createEduIDInstitutionLanding}
        <Spinner />
    {:else}
        <div class="inner">
            <h3>{I18n.t("createFromInstitution.title")}</h3>
            <div class="info">
                <p class="header">{I18n.t("createFromInstitution.header")}</p>
                <p>{I18n.t("createFromInstitution.info")}</p>
            </div>
            <div class="actions">
                <Button label={I18n.t("createFromInstitution.startFlow")}
                        large={true}
                        disabled={busy}
                        onClick={startFlow}/>
            </div>
            <div class="footer">
                <span>{@html I18n.t("createFromInstitution.alreadyHaveAnEduID", {location: $config.spBaseUrl})}</span>
            </div>
        </div>
    {/if}
</div>
