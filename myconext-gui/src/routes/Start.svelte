<script>
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import {user} from "../stores/user";
    import personalInfoSvg from "../icons/redesign/Personal_info.svg?raw";
    import dataActivitySvg from "../icons/redesign/data_activity.svg?raw";
    import securitySvg from "../icons/redesign/Security.svg?raw";
    import settingsSvg from "../icons/redesign/settings.svg?raw";
    import getApp from "../icons/redesign/undraw_Mobile_app_re_catg 1.svg?raw";
    import Button from "../components/Button.svelte";
    import {isEmpty} from "../utils/utils";
    import {startLinkAccountFlow} from "../api";
    import Modal from "../components/Modal.svelte";

    let showModal = false;

    const addInstitution = showConfirmation => {
        if (showConfirmation) {
            showModal = true
        } else {
            startLinkAccountFlow().then(json => {
                window.location.href = json.url;
            });
        }
    }


</script>

<style lang="scss">
    .start {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        margin-bottom: 80px;
    }

    h2 {
        margin: 35px 0 10px 0;
        color: var(--color-primary-green);
    }

    p.manage {
        font-weight: 300;
        margin-bottom: 26px;
    }

    .card-container {
        margin: 0 auto;
        display: grid;
        width: 100%;
        grid-template-columns: repeat(2, 1fr);
        grid-template-rows: repeat(2, 1fr);
        grid-gap: 25px;

        @media (max-width: 820px) {
            grid-template-columns: 1fr;
        }
    }

    .info-container {
        grid-column: 1 / 3;
        grid-row: 1 / 3;
        box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.5);
        padding: 15px;

        @media (max-width: 820px) {
            grid-column: 1 / 1;
        }

        h4 {
            color: var(--color-primary-green);
            margin-bottom: 25px;
        }

        .content-section {
            display: grid;
            width: 100%;
            grid-template-columns: repeat(2, 1fr);
            grid-gap: 25px;

            @media (max-width: 820px) {
                grid-template-columns: repeat(1, 1fr);
            }
        }

        .info-section {
            @media (max-width: 820px) {
                width: 100%;
            }
        }

        p.info {
            margin-bottom: 36px;

            span.info-bold {
                font-weight: 600;
            }
        }


        p.requirements {
            font-weight: 400;
            margin-bottom: 10px;
        }

        ul {
            list-style: inherit;

            li {
                margin: 0 0 5px 20px;
            }
        }

        div.image {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-left: auto;
            @media (max-width: 820px) {
                width: 100%;
            }

            :global(svg) {
                width: 260px;
                height: auto;
                margin-bottom: 20px;
            }

        }
    }

    .card {
        padding: 40px 0 25px 0;
        box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.5);
        display: flex;
        flex-direction: column;
        align-items: center;
        cursor: pointer;
        min-height: 275px;

        :global(svg) {
            width: 160px;
            height: auto;
        }

        h3 {
            margin-top: auto;
            font-size: 22px;
        }
    }


</style>
<div class="start">
    <h2>{I18n.t("start.hi", {name: $user.chosenName})}</h2>
    <p class="manage">{I18n.t("start.manage")}</p>
    <div class="card-container">
        {#if isEmpty($user.linkedAccounts)}
            <div class="info-container">
                <h4>{I18n.t("start.app.title")}</h4>
                <div class="content-section">
                    <div class="info-section">
                        <p class="info">
                            <span class="info-bold">{I18n.t("start.app.infoBold")}</span>
                            <span>{I18n.t("start.app.infoPart")}</span>
                        </p>
                        <p class="requirements">{I18n.t("start.app.requirements")}</p>
                        <ul>
                            <li>{I18n.t("start.app.validatedName")}</li>
                            <li>{I18n.t("start.app.proofStudent")}</li>
                            <li>{I18n.t("start.app.institution")}</li>
                        </ul>
                    </div>
                    <div class="image">
                        {@html getApp}
                        <div class="action">
                            <Button label={I18n.t("start.app.connect")}
                                    xxl={true}
                                    onClick={() => addInstitution(true)}/>
                        </div>
                    </div>
                </div>

            </div>
        {/if}

        <div class="card" on:click={() => navigate("/personal")}>
            {@html personalInfoSvg}
            <h3>{I18n.t("sidebar.personalInfo")}</h3>
        </div>
        <div class="card" on:click={() => navigate("/data-activity")}>
            {@html dataActivitySvg}
            <h3>{I18n.t("sidebar.dataActivity")}</h3>
        </div>
        <div class="card" on:click={() => navigate("/security")}>
            {@html securitySvg}
            <h3>{I18n.t("sidebar.security")}</h3>
        </div>
        <div class="card" on:click={() => navigate("/account")}>
            {@html settingsSvg}
            <h3>{I18n.t("sidebar.account")}</h3>
        </div>

    </div>

</div>
{#if showModal}
    <Modal submit={() => addInstitution(false)}
           cancel={() => showModal = false}
           question={I18n.t(`profile.verifyFirstAndLastName.addInstitutionConfirmation`)}
           title={I18n.t(`profile.verifyFirstAndLastName.addInstitution`)}
           confirmTitle={I18n.t("profile.proceed")}>
    </Modal>
{/if}
