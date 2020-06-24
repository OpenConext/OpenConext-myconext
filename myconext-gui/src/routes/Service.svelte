<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {deleteServiceAndTokens} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import {formatCreateDate} from "../format/date";
    import Modal from "../components/Modal.svelte";

    let service = {};
    let token = null;
    let tokens = [];
    let showModal = false;

    const useToken = allTokens => {
        if (allTokens.length === 0) {
            return null;
        }
        if (allTokens.length === 1) {
            return allTokens[0];
        }
        const refreshToken = allTokens.find(t => t.type === "REFRESH");
        return refreshToken || allTokens[0];
    }

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const eduid = urlSearchParams.get("eduid");
        const services = Object.keys($user.eduIdPerServiceProvider).map(k => ({
            entityId: k,
            name: $user.eduIdPerServiceProvider[k].serviceName,
            eduId: $user.eduIdPerServiceProvider[k].value,
            createdAt: $user.eduIdPerServiceProvider[k].createdAt
        }));
        service = services.find(o => o.eduId === eduid);
        const allTokens = $user.oidcTokens.filter(token => token.clientId === service.entityId);
        token = useToken(allTokens);
        tokens = allTokens.map(t => ({id: t.id, tokenType: t.type}));
    });

    const deleteEduId = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true;
        } else {
            deleteServiceAndTokens(service.eduId, tokens).then(json => {
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                    }
                }
                navigate("/services");
                flash.setValue(I18n.t("service.deleted", {name: service.name}));
            });
        }
    }

    const cancel = () => navigate("/services");

</script>

<style>
    .service {
        width: 100%;
        display: flex;
        height: 100%;
    }

    @media (max-width: 820px) {
        .left {
            display: none;
        }

        .inner {
            border-left: none;
        }
    }

    .header {
        display: flex;
        align-items: center;
        align-content: center;
        color: var(--color-primary-green);
    }

    .header a {
        margin-top: 8px;
    }

    h2 {
        margin-left: 25px;
    }

    p.info {
        margin: 12px 0 32px 0;
    }

    table {
        width: 100%;
    }

    td {
        border-bottom: 1px solid var(--color-primary-grey);
    }

    td.attr {
        width: 30%;
        padding: 20px;
    }

    td.value {
        width: 70%;
        font-weight: bold;
        padding-left: 20px;
    }

    div.value-container {
        display: flex;
        flex-direction: column;
    }

    div.value-container span {
        word-break: break-word;
        padding: 5px 0;
    }

    div.value-container ul {
        padding: 5px 0;
        font-weight: bold;
    }

    .options {
        margin-top: 60px;
    }

</style>
<div class="service">
    <div class="left"></div>
    <div class="inner">
        <div class="header">
            <a href="/back" on:click|preventDefault|stopPropagation={cancel}>
                {@html chevron_left}
            </a>
            <h2>{I18n.t("service.title")}</h2>
        </div>
        <p class="info">{I18n.t("service.info", formatCreateDate(service.createdAt))}</p>

        <table cellspacing="0">
            <thead></thead>
            <tbody>
            <tr class="name">
                <td class="attr">{I18n.t("service.name")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{`${service.name}`}</span>
                    </div>
                </td>
            </tr>
            <tr class="name">
                <td class="attr">{I18n.t("service.eduId")}</td>
                <td class="value">
                    <div class="value-container">
                        <span>{`${service.eduId}`}</span>
                    </div>
                </td>
            </tr>
            {#if token && token.scopes && token.scopes.find(scope => scope.name !== "openid" && scope.descriptions[I18n.locale])}
                <tr class="name">
                    <td class="attr">{I18n.t("service.access")}</td>
                    <td class="value">
                        <div class="value-container">
                            <ul>
                                {#each token.scopes.filter(sc => sc.descriptions[I18n.locale] && sc.name !== "openid") as scope}
                                    <li>{scope.descriptions[I18n.locale]}</li>
                                {/each}
                            </ul>
                        </div>
                    </td>
                </tr>
                {#if token.audiences && token.audiences.length > 0}
                    <tr class="name">
                        <td class="attr">{I18n.t("service.accounts")}</td>
                        <td class="value">
                            <div class="value-container">
                                <ul>
                                    {#each token.audiences as audience}
                                        <li>{audience}</li>
                                    {/each}
                                </ul>
                            </div>
                        </td>
                    </tr>
                {/if}
            {/if}
            </tbody>
        </table>


        <div class="options">
            <Button className="cancel" label={I18n.t("service.cancel")} onClick={cancel}/>

            <Button label={I18n.t("service.delete")} className="cancel" onClick={deleteEduId(true)}/>
        </div>
    </div>

</div>

{#if showModal}
    <Modal submit={deleteEduId(false)}
           cancel={() => showModal = false}
           question={I18n.t("service.deleteServiceConfirmation", {name: service.name})}
                   title={I18n.t("service.deleteService")}>
    </Modal>
{/if}
