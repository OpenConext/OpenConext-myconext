<script>
    import {flash, user} from "../stores/user";
    import I18n from "i18n-js";
    import {validPassword} from "../validation/regexp";
    import {resetPasswordLink, updatePassword} from "../api";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";
    import Modal from "../components/Modal.svelte";
    import {onMount} from "svelte";

    let newPassword = "";
    let confirmPassword = "";
    let passwordResetHashExpired = false;
    let usePassword = $user.usePassword;
    let showModalDeletePassword = false;
    let hash;
    let allowedNext = false;

    $: allowedNext = validPassword(newPassword) && newPassword === confirmPassword;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        hash = urlSearchParams.get("h");
    });

    const update = (overrideWarning = false) => {
        if (allowedNext) {
            updatePassword($user.id, newPassword, hash)
                .then(json => {
                    for (var key in json) {
                        if (json.hasOwnProperty(key)) {
                            $user[key] = json[key];
                        }
                    }
                    navigate("/security");
                    flash.setValue(usePassword ? I18n.t("password.updated") : I18n.t("password.set"));
                })
                .catch(() => {
                    passwordResetHashExpired = true;
                });

        }
    };

    const cancel = () => {
        navigate("/security");
    }

    const deletePasswordOption = showConfirmation => () => {
        if (showConfirmation) {
            showModalDeletePassword = true;
        } else {
            updatePassword($user.id, null, hash).then(json => {
                for (let key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                    }
                }
                navigate("/security");
                flash.setValue(I18n.t("password.deleted"));
            }).catch(() => {
                showModalDeletePassword = false;
                passwordResetHashExpired = true;
            });
        }
    }

    const resetPasswordLinkAgain = () => {
        resetPasswordLink().then(() => {
            navigate("/security");
            flash.setValue(I18n.t("password.flash.passwordLink", {name: $user.email}));
        })

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
        margin: 60px 0 20px 0;
        display: flex;

        span.first {
            margin-right: auto;
        }

    }

</style>
<div class="password">
    <h2>{usePassword ? I18n.t("password.updateTitle") : I18n.t("password.addTitle")}</h2>
    <p class="info">{I18n.t("password.passwordDisclaimer")}</p>
    <div class="error-container">
        {#if passwordResetHashExpired}
            <span class="error">{I18n.t("password.passwordResetHashExpired")}
                <a href="/reset-link" on:click|preventDefault|stopPropagation={resetPasswordLinkAgain}>
                    {I18n.t("password.passwordResetSendAgain")}
                </a>
            </span>
        {/if}
    </div>

    <input id="username" autocomplete="username email" type="hidden" name="username" value={$user.email}>

    <label for="newPassword">{I18n.t("password.newPassword")}</label>
    <input id="newPassword" type="password" autocomplete="new-password" spellcheck="false" bind:value={newPassword}>

    <label for="confirmPassword">{I18n.t("password.confirmPassword")}</label>
    <input id="confirmPassword" type="password" spellcheck="false" autocomplete="new-password"
           bind:value={confirmPassword}>

    <div class="options">
        {#if usePassword}
        <span class="first">
            <Button deletion={true} onClick={deletePasswordOption(true)}/>
        </span>
        {/if}
        <Button className="cancel" label={I18n.t("password.cancel")} onClick={cancel}/>
        <Button label={usePassword ? I18n.t("password.updateUpdate") : I18n.t("password.setUpdate")}
                onClick={update}
                disabled={!allowedNext}/>
    </div>
</div>
{#if showModalDeletePassword}
    <Modal submit={deletePasswordOption(false)}
           cancel={() => showModalDeletePassword = false}
           warning={false}
           question={I18n.t("password.deletePasswordConfirmation")}
           title={I18n.t("password.deletePassword")}>
    </Modal>
{/if}
