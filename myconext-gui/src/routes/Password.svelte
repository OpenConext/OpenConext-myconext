<script>
    import {flash, user} from "../stores/user";
    import I18n from "i18n-js";
    import {validPassword} from "../validation/regexp";
    import {forgotPasswordLink, me, updateSecurity} from "../api";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";
    import Modal from "../components/Modal.svelte";
    import {onMount} from "svelte";

    let currentPassword = "";
    let newPassword = "";
    let confirmPassword = "";
    let currentPasswordInvalid = false;
    let passwordResetHashExpired = false;
    let usePassword = $user.usePassword;
    let showModal = false;
    let hash;
    let userForgotPassword = false;
    let outstandingEmailReset = false;

    const valid = () => {
        let existingPasswordValid = usePassword && (currentPassword || userForgotPassword) && validPassword(newPassword) && newPassword === confirmPassword;
        let newPasswordValid = !usePassword && validPassword(newPassword) && newPassword === confirmPassword;
        return (existingPasswordValid || newPasswordValid);
    };

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        hash = urlSearchParams.get("h");
        userForgotPassword = (hash !== undefined && hash !== null);
    });

    const update = () => {
        if (valid()) {
            updateSecurity($user.id, currentPassword, newPassword, hash)
                .then(json => {
                    for (var key in json) {
                        if (json.hasOwnProperty(key)) {
                            $user[key] = json[key];
                        }
                    }
                    navigate("/security");
                    flash.setValue(userForgotPassword ? I18n.t("password.reset") : usePassword ? I18n.t("password.updated") : I18n.t("password.set"));
                })
                .catch(() => {
                    if (userForgotPassword) {
                        passwordResetHashExpired = true;
                    } else {
                        currentPasswordInvalid = true;
                    }
                });
        }
    };

    const cancel = () => {
        me().then(json => {
            for (var key in json) {
                if (json.hasOwnProperty(key)) {
                    $user[key] = json[key];
                }
            }
            navigate("/security");
        });
    }

    const forgotPassword = (showConfirmation, force=false) => () => {
        if (showConfirmation) {
            showModal = true;
        } else {
            forgotPasswordLink(force).then(() => {
                showModal = false;
                navigate("/security");
                flash.setValue(I18n.t("password.flash.passwordLink", {name: $user.email}));
            }).catch(e => {
                if (e.status === 406) {
                    outstandingEmailReset = true;
                    showModal = false;
                }
            });
        }
    }

</script>

<style lang="scss">
    .password {
        width: 100%;
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    h2 {
        margin-top: 35px;
        color: var(--color-primary-green);
    }

    p.info {
        margin: 22px 0 0 0;
    }

    div.error-container {
        margin-top: 10px;
        display: flex;
    }

    div.error-container a {
        margin-left: auto;
    }

    label {
        font-weight: bold;
        margin: 33px 0 13px 0;
        display: inline-block;
    }

    input {
        border-radius: 8px;
        border: solid 1px #676767;
        padding: 14px;
        font-size: 16px;
    }

    span.error {
        display: inline-block;
        color: var(--color-primary-red);
    }

    .options {
        margin-top: 60px;
    }

</style>
<div class="password">
    <h2>{userForgotPassword ? I18n.t("password.resetTitle") : usePassword ? I18n.t("password.updateTitle") : I18n.t("password.setTitle")}</h2>
    <p class="info">{I18n.t("password.passwordDisclaimer")}</p>
    {#if usePassword && !userForgotPassword}
        <label for="currentPassword">{I18n.t("password.currentPassword")}</label>
        <input id="currentPassword" autocomplete="current-password" type="password" bind:value={currentPassword}/>

    {/if}
    <div class="error-container">
        {#if currentPasswordInvalid}
            <span class="error">{I18n.t("password.invalidCurrentPassword")}</span>
        {/if}
        {#if passwordResetHashExpired}
            <span class="error">{I18n.t("password.passwordResetHashExpired")}
                <a href="/forgot" on:click|preventDefault|stopPropagation={forgotPassword(true)}>
                    {I18n.t("password.passwordResetSendAgain")}
                </a>
            </span>
        {/if}
        {#if usePassword && !passwordResetHashExpired && !userForgotPassword}
            <a class="forgot-password" href="/forgot"
               on:click|preventDefault|stopPropagation={forgotPassword(true)}>
                {I18n.t("password.forgotPassword")}
            </a>
        {/if}
    </div>

    <input id="username" autocomplete="username email" type="hidden" name="username" value={$user.email}>

    <label for="newPassword">{I18n.t("password.newPassword")}</label>
    <input id="newPassword" type="password" autocomplete="new-password" bind:value={newPassword}>

    <label for="confirmPassword">{I18n.t("password.confirmPassword")}</label>
    <input id="confirmPassword" type="password" autocomplete="new-password" bind:value={confirmPassword}>

    <div class="options">
        <Button className="cancel" label={I18n.t("password.cancel")} onClick={cancel}/>

        <Button label={usePassword ? I18n.t("password.updateUpdate") : I18n.t("password.setUpdate")}
                onClick={update}
                disabled={!((usePassword && (currentPassword || userForgotPassword) && validPassword(newPassword) && newPassword === confirmPassword) ||
                (!usePassword && validPassword(newPassword) && newPassword === confirmPassword))}/>
    </div>
</div>
{#if showModal}
    <Modal submit={forgotPassword(false)}
           cancel={() => showModal = false}
           warning={false}
           question={I18n.t("password.forgotPasswordConfirmation")}
           title={I18n.t("password.forgotPassword")}>
    </Modal>
{/if}
{#if outstandingEmailReset}
    <Modal submit={forgotPassword(false, true)}
           cancel={() => navigate("/security")}
           warning={true}
           question={I18n.t("password.outstandingEmailResetConfirmation")}
           title={I18n.t("password.outstandingEmailReset")}>
    </Modal>
{/if}