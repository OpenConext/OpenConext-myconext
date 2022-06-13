<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {me, updateUser} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";

    const update = () => {
        if ($user.familyName && $user.givenName) {
            updateUser($user).then(() => {
                navigate("/personal");
                flash.setValue(I18n.t("edit.updated"));
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
            navigate("/personal");
        });
    }

</script>

<style>
    .profile {
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
        margin: 12px 0 32px 0;
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

    .options {
        margin-top: 60px;
        display: flex;
    }

</style>
<div class="profile">
    <h2>{I18n.t("edit.title")}</h2>
    <p class="info">{I18n.t("edit.info")}</p>
    <label for="givenName">{I18n.t("edit.givenName")}</label>
    <input id="givenName" type="text" bind:value={$user.givenName}/>
    <label for="familyName">{I18n.t("edit.familyName")}</label>
    <input id="familyName" type="text" bind:value={$user.familyName}/>

    <div class="options">
        <Button className="cancel" label={I18n.t("edit.cancel")} onClick={cancel}/>
        <Button label={I18n.t("edit.update")} onClick={update}
                disabled={!($user.familyName && $user.givenName)}/>
    </div>
</div>
