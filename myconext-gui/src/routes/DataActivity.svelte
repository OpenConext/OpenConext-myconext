<script>
    import {user} from "../stores/user";
    import I18n from "i18n-js";
    import {onMount} from "svelte";
    import {serviceName} from "../utils/services";
    import chevronDownSvg from "../icons/chevron-down.svg";
    import chevronUpSvg from "../icons/chevron-up.svg";
    import informationalSvg from "../icons/informational.svg";
    import {formatCreateDate} from "../format/date";

    const serviceDetails = service => () => {
        showDetails = {...showDetails, [service.eduId]: !showDetails[service.eduId]};
    }

    let services = [];
    let showDetails = {};

    const refresh = () => {
        services = Object.keys($user.eduIdPerServiceProvider).map(k => {
            const service = $user.eduIdPerServiceProvider[k];
            const tokens = $user.oidcTokens
                .filter(token => token.clientId === k)
                .filter(token => token.scopes && token.scopes.find(scope => scope.name !== "openid" && scope.descriptions[I18n.locale]));
            const token = tokens.length === 0 ? null : (tokens.find(t => t.type === "REFRESH") || tokens[0]);
            return {
                name: serviceName(service),
                eduId: service.value,
                createdAt: formatCreateDate(service.createdAt).date,
                data: $user.eduIdPerServiceProvider[k],
                token: token
            }
        });
        showDetails = Object.keys($user.eduIdPerServiceProvider).reduce((acc, key) => {
            const service = $user.eduIdPerServiceProvider[key];
            acc[service.eduId] = false;
            return acc
        }, {});

    }

    onMount(() => refresh());


</script>

<style lang="scss">
    .services {
        width: 100%;
        height: 100%;
    }

    .inner-container {
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

    p {
        line-height: 1.33;
        letter-spacing: normal;

        &.no-services {
            margin-top: 25px;
            font-style: italic;
        }

        &.info {
            font-weight: 300;
            margin-bottom: 26px;
        }
    }

    table {
        margin-top: 30px;
        width: 100%;

        tr {
            cursor: pointer;
        }

        td {
            border-bottom: 1px solid var(--color-primary-grey);

            &.first {
                border-top: 2px solid var(--color-primary-blue);
            }

            &.last {
                border-bottom: none;
            }

            &.value {
                width: 70%;
                font-weight: bold;
                padding: 20px;
            }
        }

        div.value-container {
            display: flex;
            align-items: center;
        }

        div.value-container span {
            word-break: break-word;
        }

        div.value-container-inner {
            margin-left: auto;
            display: flex;
            align-items: center;
            align-content: center;
        }

        div.value-container a.toggle-link {
            margin-left: 15px;
        }

        div.access {
            background-color: var(--color-secondary-blue);
            padding: 5px 10px;
            display: flex;
            align-items: center;
            align-content: center;
            border-radius: 8px;
            margin-left: auto;

            span.svg-informational {
                margin-left: 1px;
            }

            span.access {
                margin-left: 15px;
            }
        }

    }


    :global(a.toggle-link svg) {
        fill: var(--color-primary-grey);
    }

    :global(div.access span.svg-informational svg) {
        width: 26px;
        height: auto;
    }


</style>
<div class="services">
    <div class="inner-container">
        <h2>{I18n.t("dataActivity.title")}</h2>
        <p class="info">{I18n.t("dataActivity.info")}</p>
        {#if services.length === 0}
            <p class="no-services">{I18n.t("dataActivity.noServices")}</p>
        {:else}
            <h4>{I18n.t("dataActivity.explanation")}</h4>
            <table cellspacing="0">
                <thead></thead>
                <tbody>
                {#each services as service, i}
                    <tr class="name"
                        class:full={showDetails[service.eduId]}
                        on:click={serviceDetails(service)}>
                        <td class="value" class:first={i === 0} class:last={i === services.length - 1}>
                            <div class="value-container">
                                <span>{`${service.name}`}</span>
                                <div class="value-container-inner">
                                    {#if service.token}
                                        <div class="access">
                                            <span class="svg-informational">{@html informationalSvg}</span>
                                            <span class="access">{I18n.t("dataActivity.access")}</span>
                                        </div>
                                    {/if}
                                    <a class="toggle-link" href="/edit"
                                       on:click|preventDefault|stopPropagation={serviceDetails(service)}>
                                        {@html showDetails[service.eduId] ? chevronUpSvg : chevronDownSvg}
                                    </a>
                                </div>
                            </div>
                        </td>
                    </tr>
                    {#if showDetails[service.eduId]}
                        <tr class="full">
                            <table class="inner-details">
                                <thead></thead>
                                <tbody>
                                    <tr>
                                        <td colspan="2" class="details">{I18n.t("dataActivity.")}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </tr>
                    {/if}
                {/each}
                </tbody>
            </table>
        {/if}

    </div>

</div>
