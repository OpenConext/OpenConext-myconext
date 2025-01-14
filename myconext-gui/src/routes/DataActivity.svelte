<script>
    import {user} from "../stores/user";
    import I18n from "../locale/I18n";
    import {onMount} from "svelte";
    import {serviceName} from "../utils/services";
    import chevronDownSvg from "../icons/chevron-down.svg?raw";
    import chevronUpSvg from "../icons/chevron-up.svg?raw";
    import informationalSvg from "../icons/informational.svg?raw";
    import {formatOptions} from "../format/date";
    import Service from "./Service.svelte";

    const serviceDetails = service => () => {
        showDetails = {...showDetails, [service.entityId]: !showDetails[service.entityId]};
    }

    let services = [];
    let showDetails = {};

    const refresh = () => {
        services = Object.keys($user.eduIdPerServiceProvider).reduce((acc, k) => {
            const service = $user.eduIdPerServiceProvider[k];
            const allTokens = $user.oidcTokens
                .filter(token => token.clientId === k);
            const tokens = allTokens
                .filter(token => token.scopes && token.scopes.find(scope => scope.name !== "openid" && (
                    scope.descriptions[I18n.currentLocale()] || scope.descriptions["en"])));
            const token = tokens.length === 0 ? null : (tokens.find(t => t.type === "REFRESH") || tokens[0]);
            const seen = new Set();
            const scopes = allTokens.reduce((acc, token) => {
                token.scopes.forEach(scope => {
                    if (!seen.has(scope.name)) {
                        seen.add(scope.name);
                        acc.push(scope);
                    }
                })
                return acc;
            }, []);
            const locale = I18n.currentLocale() === "en" ? "en-US" : "nl-NL";
            //We don't need backward compatibility as every eduID which comes from the server is migrated
            service.services.forEach(s => {
                acc.push({
                    name: serviceName(s),
                    eduId: service.value,
                    entityId: k,
                    createdAt: new Date(s.createdAt).toLocaleDateString(locale, formatOptions),
                    lastLogin: s.lastLogin ? new Date(s.lastLogin).toLocaleDateString(locale, formatOptions) : null,
                    expiresIn: new Date(service.expiresIn).toLocaleDateString(locale, formatOptions),
                    data: {
                        serviceLogoUrl: s.logoUrl,
                        serviceProviderEntityId: k,
                        serviceHomeUrl: s.homeUrl,
                        value: service.value
                    },
                    token: token,
                    tokens: tokens,
                    allTokens: allTokens,
                    scopes: scopes
                });
            });
            return acc;
        },[]);
        showDetails = Object.keys($user.eduIdPerServiceProvider).reduce((acc, key) => {
            const service = $user.eduIdPerServiceProvider[key];
            acc[service.entityId] = false;
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

    table.data-activity {
        margin-top: 30px;
        width: 100%;

        tr {
            cursor: pointer;

            &:hover {
                background-color: var(--color-background);
            }

            &.full {
                background-color: var(--color-background);
            }

        }

        td {
            padding: 15px 0 15px 10px;

            border-bottom: 1px solid var(--color-primary-grey);

            &.first {
                border-top: 2px solid var(--color-primary-blue);
            }

            @media (max-width: 800px) {
                &.first {
                    border-top: 1px solid var(--color-primary-blue);
                }

            }

            &.last {
                border-bottom: none;
            }

            &.value {
                font-weight: bold;
            }

            &.logo {
                width: 72px;

                img {
                    width: 72px;
                    height: auto;
                }
            }
        }

        div.value-container {
            display: flex;
            align-items: center;
        }

        @media (max-width: 800px) {
            div.value-container {
                flex-direction: column;
                align-items: flex-start;
            }
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

        @media (max-width: 800px) {
            div.value-container-inner {
                margin-top: 15px;
                margin-left: 0;
            }
        }

        div.value-container a.toggle-link {
            margin-left: 22px;
        }

        div.access {
            background-color: var(--color-secondary-blue);
            padding: 5px 10px;
            display: flex;
            align-items: center;
            align-content: center;
            border-radius: 8px;
            margin-left: auto;
            font-weight: normal;

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
        <h2>{I18n.t("DataActivity.Title.COPY")}</h2>
        <p class="info">{I18n.t("dataActivity.info")}</p>
        {#if services.length === 0}
            <p class="no-services">{I18n.t("DataActivity.NoServices.COPY")}</p>
        {:else}
            <h4>{I18n.t("DataActivity.ExplainIcon.COPY")}</h4>
            <table cellspacing="0" class="data-activity">
                <thead></thead>
                <tbody>
                {#each services as service, i}
                    <tr class="name"
                        class:full={showDetails[service.entityId]}
                        on:click={serviceDetails(service)}>
                        <td class="logo" class:first={i === 0}
                            class:last={i === services.length - 1 || showDetails[service.entityId]}>
                            {#if service.data.serviceLogoUrl}
                                <span><img src={service.data.serviceLogoUrl} alt=""></span>
                            {/if}
                        </td>
                        <td class="value" class:first={i === 0}
                            class:last={i === services.length - 1 || showDetails[service.entityId]}>
                            <div class="value-container">
                                <span>{`${service.name}`}</span>
                                <div class="value-container-inner">
                                    {#if service.token}
                                        <div class="access">
                                            <span class="svg-informational">{@html informationalSvg}</span>
                                            <span class="access">{I18n.t("DataActivity.Access.COPY")}</span>
                                        </div>
                                    {/if}
                                    <a class="toggle-link" href="/edit"
                                       on:click|preventDefault|stopPropagation={serviceDetails(service)}>
                                        {@html showDetails[service.entityId] ? chevronUpSvg : chevronDownSvg}
                                    </a>
                                </div>
                            </div>
                        </td>
                    </tr>
                    {#if showDetails[service.entityId]}
                        <Service service={service} refresh={refresh}/>
                    {/if}
                {/each}
                </tbody>
            </table>
        {/if}

    </div>

</div>
