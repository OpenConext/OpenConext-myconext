<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {me, updateUser} from "../api";
    import {navigate} from "svelte-routing";

    const update = () => {
        if ($user.familyName && $user.givenName) {
            updateUser($user).then(() => {
                $flash = I18n.ts("edit.updated");
                navigate("/profile");
            });
        }
    };

    const cancel = () => {
        me().then(json => {
            $user = {$user, ...json};
            navigate("/profile");
        });
    }

</script>

<style>
    .profile {
        width: 100%;
    }
    .inner {
        max-width: 680px;
        margin: 0 auto;
        padding: 15px 20px;
        font-size: 18px;
        display: flex;
        flex-direction: column;
    }
    h2 {
        font-size: 22px;
        margin-bottom: 20px;
        display: flex;
        align-items: center;

    }
    label {
        font-weight: 300;
        margin-right: 20px;
        width: 220px;
    }
    input[type=text] {
        border: 1px solid #dadce0;
        border-radius: 4px;
        font-weight: 300;
        padding: 6px;
        margin: 5px 0;
        font-size: 18px;
        flex-grow: 2;
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
    .button:last-child {
        margin-left: 95px;
    }
    div.form-field {
        display: flex;
        width: 100%;
        align-items: center;
        margin-bottom: 15px;
    }
    div.options {
        display: flex;
        margin-top: 60px;
        align-content: space-between;
    }
    a.back  {
        text-decoration: none;
        font-size: 32px;
        color: #007bff;
        display: inline-block;
        margin-right: 15px;

    }
</style>
<div class="profile">
    <div class="inner">
        <h2> <a class="back" href="/{I18n.ts('edit.back')}"
                on:click|preventDefault|stopPropagation={cancel}>
            ←
        </a>{I18n.ts("edit.title")}</h2>
        <div class="form-field">
            <label for="givenName">{I18n.ts("edit.givenName")}</label>
            <input id="givenName" type="text" bind:value={$user.givenName}/>
        </div>
        <div class="form-field">
            <label for="familyName">{I18n.ts("edit.familyName")}</label>
            <input id="familyName" type="text" bind:value={$user.familyName}/>
        </div>

        <div class="options">
            <a class="button" href="/cancel"
               on:click|preventDefault|stopPropagation={cancel}>
                {I18n.ts("edit.cancel")}
            </a>
            <a class="button" href="/update"
               class:disabled={!($user.familyName && $user.givenName)}
               on:click|preventDefault|stopPropagation={update}>
                {I18n.ts("edit.update")}
            </a>
        </div>
    </div>

</div>