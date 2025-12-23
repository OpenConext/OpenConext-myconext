<script>
    import I18n from "../locale/I18n";
    import {Link} from "svelte-routing";
    import {user, config} from "../stores/user";
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

    :global(.card) {
        padding: 40px 0 25px 0;
        box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.5);
        display: flex;
        flex-direction: column;
        align-items: center;
        cursor: pointer;
        min-height: 275px;
        text-decoration: none;
        color: inherit;

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
    <h2>{I18n.t("Start.Hi.COPY", {name: $user.chosenName})}</h2>
    <p class="manage">{I18n.t("Start.Manage.COPY")}</p>
    <div class="card-container">
        {#if $config.enableAccountLinking && isEmpty($user.linkedAccounts)}
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
                            <li>{I18n.t("CreateEduID.FirstTimeDialog.MainTextPoint1.COPY")}</li>
                            <li>{I18n.t("CreateEduID.FirstTimeDialog.MainTextPoint2.COPY")}</li>
                            <li>{I18n.t("CreateEduID.FirstTimeDialog.MainTextPoint3.COPY")}</li>
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

        <Link to="/personal" class="card">
            {@html personalInfoSvg}
            <h3>{I18n.t("Sidebar.PersonalInfo.COPY")}</h3>
        </Link>
        <Link to="/data-activity" class="card">
            {@html dataActivitySvg}
            <h3>{I18n.t("Sidebar.DataActivity.COPY")}</h3>
        </Link>
        <Link to="/security" class="card">
            {@html securitySvg}
            <h3>{I18n.t("Sidebar.Security.COPY")}</h3>
        </Link>
        <Link to="/account" class="card">
            {@html settingsSvg}
            <h3>{I18n.t("Sidebar.Account.COPY")}</h3>
        </Link>

    </div>

</div>
{#if showModal}
    <Modal submit={() => addInstitution(false)}
           cancel={() => showModal = false}
           question={I18n.t(`Profile.VerifyFirstAndLastName.AddInstitutionConfirmation.COPY`)}
           title={I18n.t(`Profile.VerifyFirstAndLastName.AddInstitution.COPY`)}
           confirmTitle={I18n.t("Profile.Proceed.COPY")}>
    </Modal>
{/if}
