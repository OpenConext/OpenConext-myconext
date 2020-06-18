<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {me, deleteLinkedAccount, deleteService} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import {formatCreateDate} from "../format/date";
    import Modal from "../components/Modal.svelte";

    let service = {};
    let showModal = false;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const eduid = urlSearchParams.get("eduid");
        const services = Object.keys($user.eduIdPerServiceProvider).map(k => ({
            name: k,
            eduId: $user.eduIdPerServiceProvider[k].value,
            createdAt: $user.eduIdPerServiceProvider[k].createdAt
        }));
        service = services.find(o => o.eduId === eduid);
    });

    const deleteEduId = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true;
        } else {
            deleteService(service).then(json => {
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

    .left {
        background-color: #f3f6f8;
        width: 270px;
        height: 100%;
        border-bottom-left-radius: 8px;
    }

    .inner {
        margin: 20px 0 190px 0;
        padding: 15px 15px 0 40px;
        border-left: 2px solid var(--color-primary-grey);
        display: flex;
        flex-direction: column;
        background-color: white;
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
        padding: 5px 0;
    }
    .options {
        margin-top: 60px;
    }

    :global(.options a:not(:first-child)) {
        margin-left: 25px;
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
