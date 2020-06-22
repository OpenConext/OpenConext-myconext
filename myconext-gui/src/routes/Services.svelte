<script>
    import {user} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import chevron_right from "../icons/chevron-right.svg";
    import {onMount} from "svelte";

    const serviceDetails = eduid => () =>
            navigate(`/service?eduid=${encodeURIComponent(eduid)}`);

    let services = [];

    onMount(() => services = Object.keys($user.eduIdPerServiceProvider).map(k => ({
        name: $user.eduIdPerServiceProvider[k].serviceName,
        eduId: $user.eduIdPerServiceProvider[k].value
    })));

</script>

<style>
    .services {
        width: 100%;
        height: 100%;
    }

    .inner {
        height: 100%;
        margin: 0 auto;
        padding: 15px 30px 15px 0;
        display: flex;
        flex-direction: column;
    }

    h2 {
        margin: 20px 0 10px 0;
        color: var(--color-primary-green);
    }

    p.info {
        font-weight: 300;
        margin-bottom: 26px;
    }

    p {
        font-size: 18px;
        line-height: 1.33;
        letter-spacing: normal;
    }

    p.no-services {
        margin-top: 25px;
        font-style: italic;
    }

    table {
        margin-top: 30px;
        width: 100%;
    }

    tr {
        cursor: pointer;
    }

    td {
        border-bottom: 1px solid var(--color-primary-grey);
    }

    td.last {
        border-bottom: none;
    }

    td.value {
        width: 70%;
        font-weight: bold;
        padding: 20px;
    }

    div.value-container {
        display: flex;
        align-items: center;
    }

    div.value-container a.menu-link {
        margin-left: auto;
    }

    :global(a.menu-link svg) {
        fill: var(--color-primary-green);
    }


</style>
<div class="services">
    <div class="inner">
        <h2>{I18n.t("services.title")}</h2>
        <p class="info">{I18n.t("services.info")}</p>
        {#if services.length === 0}
            <p class="no-services">{I18n.t("services.noServices")}</p>
        {:else}
            <p class="info">{I18n.t("services.explanation")}</p>
            <table cellspacing="0">
                <thead></thead>
                <tbody>
                {#each services as service, i}
                    <tr class="name" on:click={serviceDetails(service.eduId)}>
                        <td class="value" class:last={i === services.length - 1}>
                            <div class="value-container">
                                <span>{`${service.name}`}</span>
                                <a class="menu-link" href="/edit"
                                   on:click|preventDefault|stopPropagation={serviceDetails(service.eduId)}>
                                    {@html chevron_right}
                                </a>
                            </div>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        {/if}

    </div>

</div>
