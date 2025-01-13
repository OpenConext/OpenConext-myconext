<script>
    import {flash, user} from "../stores/user";
    import I18n from "i18n-js";
    import {deleteServiceAndTokens, deleteTokens, oidcTokens} from "../api";
    import Button from "../components/Button.svelte";
    import Modal from "../components/Modal.svelte";
    import {formatJsDate} from "../format/date";
    import Spinner from "../components/Spinner.svelte";

    export let service = {data: {}};
    export let refresh;

    let showModal = false;
    let modalOptions = {};
    let loading = false;

    const modalDeleteEduId = () => ({
        submit: deleteEduId(false),
        question: I18n.t("dataActivity.deleteServiceConfirmation", {name: service.name}),
        title: I18n.t("dataActivity.deleteService")
    });

    const modalRevokeTokens = () => ({
        submit: revokeTokens(false),
        question: I18n.t("dataActivity.deleteTokenConfirmation", {name: service.name}),
        title: I18n.t("dataActivity.deleteToken")
    });

    const doRefresh = (json, flashMsg) => {
        for (var key in json) {
            if (json.hasOwnProperty(key)) {
                $user[key] = json[key];
            }
        }
        oidcTokens().then(res => {
            $user.oidcTokens = res;
            loading = false;
            flash.setValue(I18n.t(flashMsg, {name: service.name}));
            refresh();
        })
    }

    const deleteEduId = showConfirmation => () => {
        if (showConfirmation) {
            modalOptions = modalDeleteEduId();
            showModal = true;
        } else {
            loading = true;
            showModal = false;
            deleteServiceAndTokens(service.entityId, service.allTokens)
                .then(res => {
                    doRefresh(res, "dataActivity.deleted");
                });
        }
    }

    const revokeTokens = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true;
            modalOptions = modalRevokeTokens();
        } else {
            loading = true;
            showModal = false;
            deleteTokens(service.allTokens)
                .then(res => {
                    doRefresh(res, "dataActivity.tokenDeleted");
                });
        }
    }

</script>

<style lang="scss">
    tr.full {
        background-color: var(--color-background);
    }

    table.inner-details {
        width: 100%;
        table-layout: auto;

        td {
            border-bottom: 1px solid var(--color-primary-grey);
            padding: 15px 10px;

            &.details {
                font-weight: 600;

                div.content {
                    display: flex;
                    align-items: center;
                    span.button {
                        margin-left: auto;
                    }
                }

                @media (max-width: 800px) {
                    div.content {
                        flex-direction: column;
                        align-items: flex-start;
                        span.button {
                            margin-top: 15px;
                            margin-left: 0;
                        }
                    }
                }

            }

            &.attr {
                width: 38%;
            }

            &.value {
                width: 62%;
                font-weight: 600;

                ul {
                    list-style: disc outside none;
                    margin-left: 18px;

                    li {
                        font-weight: 600;

                        &:not(:last-child) {
                            margin-bottom: 5px;
                        }
                    }
                }
            }

            &.disclaimer {
                font-size: 13px;
                border-bottom: none;
                padding-top: 2px;

                div.content {
                    display: flex;
                }
            }

            &.last {
                border-bottom: 1px solid var(--color-secondary-grey);
            }
        }

    }


</style>
{#if loading}
    <Spinner/>
    {:else}
<tr class="full">
    <td colspan="2">
        <table class="inner-details" cellspacing="0">
            <thead></thead>
            <tbody>
            <tr>
                <td class="details" colspan="2">
                    <div class="content">
                        <span>{I18n.t("dataActivity.details.login")}</span>
                        <span class="button">
                            <Button onClick={deleteEduId(true)}
                                    large={true}
                                    inline={true}
                                    label={I18n.t("dataActivity.details.delete")}/>
                        </span>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="attr">{I18n.t("dataActivity.details.first")}</td>
                <td class="value">{service.createdAt}</td>
            </tr>
            {#if service.lastLogin }
                <tr>
                    <td class="attr">{I18n.t("dataActivity.details.last")}</td>
                    <td class="value">{service.lastLogin}</td>
                </tr>
            {/if}
            <tr>
                <td class="attr" class:last={!service.data.serviceHomeUrl}>{I18n.t("dataActivity.details.eduID")}</td>
                <td class="value" class:last={!service.data.serviceHomeUrl}>{service.data.value}</td>
            </tr>
            {#if service.data.serviceHomeUrl}
                <tr>
                    <td class="attr last">{I18n.t("dataActivity.details.homePage")}</td>
                    <td class="value last"><a href={service.data.serviceHomeUrl}
                                              target="_blank">{service.data.serviceHomeUrl}</a></td>
                </tr>
            {/if}
            <tr>
                <td colspan="2" class="disclaimer">
                    <div class="content">
                        <span><sup>*</sup> </span>
                        <span>{I18n.t("dataActivity.details.deleteDisclaimer")}</span>
                    </div>
                </td>
            </tr>
            {#if service.token}
                <tr>
                    <td class="details" colspan="2">
                        <div class="content">
                            <span>{I18n.t("dataActivity.details.access")}</span>
                            <span class="button">
                                <Button onClick={revokeTokens(true)}
                                        large={true}
                                        inline={true}
                                        label={I18n.t("dataActivity.details.revoke")}/>
                            </span>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="attr">{I18n.t("dataActivity.details.details")}</td>
                    <td class="value">
                        <ul>
                            {#each service.scopes as scope}
                                {#if scope.descriptions[I18n.locale] || scope.descriptions["en"]}
                                    <li>{scope.descriptions[I18n.locale] || scope.descriptions["en"]}</li>
                                {/if}
                            {/each}
                        </ul>
                    </td>

                </tr>
                <tr>
                    <td class="attr">{I18n.t("dataActivity.details.consent")}</td>
                    <td class="value">{formatJsDate(service.token.createdAt)}</td>
                </tr>
                <tr>
                    <td class="attr last">{I18n.t("dataActivity.details.expires")}</td>
                    <td class="value last">
                        {formatJsDate(service.token.expiresIn)}</td>
                </tr>
            {/if}
            </tbody>

        </table>
    </td>
</tr>
{/if}

{#if showModal}
    <Modal submit={modalOptions.submit}
           cancel={() => showModal = false}
           warning={true}
           confirmTitle={I18n.t("modal.delete")}
           question={modalOptions.question}
           title={modalOptions.title}>
    </Modal>
{/if}
