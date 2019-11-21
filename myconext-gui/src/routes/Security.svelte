<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {validPassword} from "../validation/regexp";
    import {updateSecurity} from "../api";

    let currentPassword = "", newPassword= "", confirmPassword = "";
    let usePassword = $user.usePassword;

    const valid = () => (usePassword && validPassword(newPassword) && newPassword === confirmPassword) || !usePassword;

    const update = () => {
        debugger;
        if (valid()) {
            updateSecurity($user.id, usePassword, !usePassword, currentPassword, newPassword).then(json => {
                $flash = I18n.t("security.updated");
                currentPassword = "";;
                        newPassword= "";
                        confirmPassword = "";
                $user = {$user, ...json}
                usePassword = $user.usePassword;
            });
        }
    };


</script>

<style>
    .settings {
    }

    .inner {
        max-width: 1080px;
        margin: 0 auto;
        padding: 60px 20px 0;
        font-size: 20px;
        display: flex;
        flex-direction: column;
    }

    label {
        font-weight: 300;
        margin-top: 15px;
    }

    em {
        font-weight: 300;
        font-size: 14px;
    }

    input[type=password] {
        border: 1px solid #dadce0;
        border-radius: 4px;
        padding: 10px;
        font-size: larger;
        width: 100%;
        margin: 5px 0;
    }

    .button {
        border: 1px solid #818181;
        width: 100%;
        background-color: #c7c7c7;
        border-radius: 2px;
        padding: 10px 20px;
        display: inline-block;
        color: black;
        text-decoration: none;
        cursor: pointer;
        text-align: center;
    }

    div.checkbox {
        display: flex;
        align-items: center;
        margin: 15px 0;
    }

    div.checkbox input {
        margin: 0 10px 0 0;
        font-size: 36px;
        cursor: pointer;
    }

    div.checkbox label {
        cursor: pointer;
        margin-top: 0;
    }


</style>
<div class="settings">
    <div class="inner">
        <div class="checkbox">
            <input type="radio"
                   id="useMagicLink"
                   bind:group={usePassword}
                   value={false}/>
            <label for="useMagicLink">{I18n.t("security.useMagicLink")}</label>
        </div>

        <div class="checkbox">
            <input type="radio"
                   id="usePassword"
                   bind:group={usePassword}
                   value={true}/>
            <label for="usePassword">{I18n.t("security.usePassword")}</label>
        </div>
        {#if $user.usePassword && usePassword}
            <label for="currentPassword">{I18n.t("security.currentPassword")}</label>
            <input id="currentPassword" type="password" bind:value={currentPassword}>
        {/if}

        {#if usePassword}
            <label for="newPassword">{I18n.t("security.newPassword")}</label>
            <input id="newPassword" type="password" bind:value={newPassword}>
            <em>{I18n.t("security.passwordDisclaimer")}</em>

            <label for="confirmPassword">{I18n.t("security.confirmPassword")}</label>
            <input id="confirmPassword" type="password" bind:value={confirmPassword}>
        {/if}


        <div class="options">
            <a class="button" href="/magic"
               class:disabled={!valid()}
               on:click|preventDefault|stopPropagation={update}>
                {I18n.t("security.update")}
            </a>
        </div>

    </div>

</div>