<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {validPassword} from "../validation/regexp";
    import {me, updateSecurity} from "../api";
    import {navigate} from "svelte-routing";

    let currentPassword = "";
    let newPassword = "";
    let confirmPassword = "";
    let currentPasswordInvalid = false;
    let usePassword = $user.usePassword;

    const valid = () => {
        let existingPasswordValid = usePassword && currentPassword && validPassword(newPassword) && newPassword === confirmPassword;
        let newPasswordValid = !usePassword && validPassword(newPassword) && newPassword === confirmPassword;
        return (existingPasswordValid || newPasswordValid);
    };

    const update = () => {
        if (valid()) {
            updateSecurity($user.id, currentPassword, newPassword)
                    .then(json => {
                        $user = {$user, ...json};
                        $flash = usePassword ? I18n.ts("password.updated") : I18n.ts("password.set");
                        navigate("/security");
                    })
                    .catch(() => {
                        currentPasswordInvalid = true;
                    });
        }
    };
    const cancel = () => {
        me().then(json => {
            $user = {$user, ...json};
            navigate("/security");
        });
    }


</script>

<style>
    .password {
        width: 100%;
    }

    .inner {
        max-width: 680px;
        margin: 0 auto;
        padding: 15px 0;
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

    p {
        font-weight: 300;
        margin-bottom: 20px;
    }

    label {
        font-weight: 300;
        margin-right: 20px;
        width: 120px;
    }

    input[type=password] {
        border: 1px solid #dadce0;
        border-radius: 4px;
        font-weight: 300;
        padding: 6px;
        margin: 5px 0;
        font-size: 18px;
        flex-grow: 2;
    }

    div.form-field {
        display: flex;
        width: 100%;
        align-items: center;
        margin-bottom: 15px;
        position: relative;
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

    div.options {
        display: flex;
        margin-top: 60px;
        align-content: space-between;
    }

    @media (max-width: 720px) {
        .inner {
            margin: 0 15px;
        }

        div.form-field {
            flex-direction: column;
            align-items: flex-start;
        }
    }

    span.error {
        display: inline-block;
        margin: 0 auto 10px 0;
        color: #d00000;
    }

    a.back {
        text-decoration: none;
        font-size: 32px;
        color: #007bff;
        display: inline-block;
        margin-right: 15px;
    }


</style>
<div class="password">
    <div class="inner">

        <h2><a class="back" href="/{I18n.ts('password.back')}"
               on:click|preventDefault|stopPropagation={cancel}>
            ←
        </a>{usePassword ? I18n.ts("password.updateTitle") : I18n.ts("password.setTitle")}</h2>
        <p>{I18n.ts("password.passwordDisclaimer")}</p>
        {#if usePassword}
            <div class="form-field">
                <label for="currentPassword">{I18n.ts("password.currentPassword")}</label>
                <input id="currentPassword" type="password" bind:value={currentPassword}>
            </div>
        {/if}
        {#if currentPasswordInvalid}
            <span class="error">{I18n.ts("password.invalidCurrentPassword")}</span>
        {/if}
        <div class="form-field">
            <label for="newPassword">{I18n.ts("password.newPassword")}</label>
            <input id="newPassword" type="password" bind:value={newPassword}>
        </div>
        <div class="form-field">
            <label for="confirmPassword">{I18n.ts("password.confirmPassword")}</label>
            <input id="confirmPassword" type="password" bind:value={confirmPassword}>
        </div>
        <div class="options">
            <a class="button" href="/cancel"
               on:click|preventDefault|stopPropagation={cancel}>
                {I18n.ts("password.cancel")}
            </a>
            <a class="button" href="/update"
               class:disabled={!((usePassword && currentPassword && validPassword(newPassword) && newPassword === confirmPassword) ||
                (!usePassword && validPassword(newPassword) && newPassword === confirmPassword))}
               on:click|preventDefault|stopPropagation={update}>
                {usePassword ? I18n.ts("password.updateUpdate") : I18n.ts("password.setUpdate")}
            </a>
        </div>

    </div>

</div>