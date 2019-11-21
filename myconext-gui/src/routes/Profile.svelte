<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {updateUser} from "../api";

    const update = () => {
        if ($user.familyName && $user.givenName) {
            updateUser($user).then(() => $flash = I18n.t("profile.updated"));
        }
    };

</script>

<style>
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
    input[type=text] {
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

    .button.disabled {
        border: none;
        cursor: not-allowed;
        color: #c7c7c7;
        background-color: #f3f3f3;
    }

</style>
<div class="profile">
    <div class="inner">
        <label for="givenName">{I18n.t("profile.givenName")}</label>
        <input id="givenName" type="text" bind:value={$user.givenName}>

        <label for="familyName">{I18n.t("profile.familyName")}</label>
        <input id="familyName" type="text" bind:value={$user.familyName}>

        <div class="options">
            <a class="button" href="/magic"
               class:disabled={!$user.familyName && !$user.givenName}
               on:click|preventDefault|stopPropagation={update}>
                {I18n.t("profile.update")}
            </a>
        </div>

    </div>

</div>