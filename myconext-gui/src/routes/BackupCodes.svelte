<script>
    import {user} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";
    import Flash from "../components/Flash.svelte";

    const doStartTiqrAuthentication = () => navigate("/use-app");

    const cancel = () => navigate("/security");

</script>

<style lang="scss">
    .backup-codes {
        width: 100%;
        display: flex;
        flex-direction: column;
        height: 100%;
        padding: 15px 30px 15px 0;
    }

    h2 {
        padding: 20px 0 10px 0;
        color: var(--color-primary-green);
    }

    p.info {
        margin-top: 32px;
    }

    .options {
        margin-top: 60px;
        display: flex;
    }

    .tiqr-app {
        background-color: white;
        display: flex;
        padding: 15px;
        border: 1px solid var(--color-secondary-grey);
        margin: 20px 0 20px 0;
        border-radius: 8px;

        @media (max-width: 820px) {
            flex-direction: column;

        }

        table {
            width: 100%;

            td {
                border-bottom: 1px solid var(--color-primary-grey);

                &.last {
                    border-bottom: none;
                }

            }

            td.attr {
                width: 43%;
                padding: 20px 25px 20px 10px;
            }

            td.value {
                width: 57%;
                font-weight: 600;
            }
        }
    }

</style>
<div class="backup-codes">
    <Flash/>
    <h2>{I18n.t("backupCodes.title")}</h2>
    <p class="info">{I18n.t("backupCodes.info")}</p>
    <div class="tiqr-app">
        <table cellspacing="0" class="no-bottom-margin">
            <thead></thead>
            <tbody>
            <tr>
                <td class="attr">{I18n.t("security.tiqr.backupMethod")}</td>
                <td class="value">{I18n.t(`security.tiqr.${$user.registration.recoveryCode ? "code" : "sms"}`)}</td>
            </tr>
            {#if $user.registration.phoneVerified}
                <tr>
                    <td class="attr last">{I18n.t("backupCodes.phoneNumber")}</td>
                    <td class="value last">{`** ** *** ${$user.registration.phoneNumber}`}</td>
                </tr>
            {:else}
                <tr>
                    <td class="attr last">{I18n.t("backupCodes.code")}</td>
                    <td class="value last">{"**** ****"}</td>
                </tr>
            {/if}
            </tbody>
        </table>
    </div>
    <div class="options">
        <Button className="cancel" label={I18n.t("password.cancel")} onClick={cancel}/>
        <Button label={I18n.t("backupCodes.startTiqrAuthentication")}
                onClick={doStartTiqrAuthentication}/>
    </div>
</div>