<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {me, updateEmail, updateUser} from "../api";
    import {navigate} from "svelte-routing";

    const {validEmail} = require("../validation/regexp");
    import Button from "../components/Button.svelte";

    let verifiedEmail = "";

    const update = () => {
        if (verifiedEmail) {
            updateEmail({...$user, email: verifiedEmail}).then(() => {
                navigate("/personal");
                flash.setValue(I18n.t("email.updated", {email: verifiedEmail}));
            });
        }
    };

    const cancel = () => navigate("/personal");

</script>

<style>
    .email {
        width: 100%;
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    h2 {
        margin-top: 25px;
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
    }

</style>
<div class="email">
    <h2>{I18n.t("email.title")}</h2>
    <p class="info">{I18n.t("email.info")}</p>
    <label for="verifiedEmail">{I18n.t("email.email")}</label>
    <input id="verifiedEmail" type="text" bind:value={verifiedEmail}/>

    <div class="options">
        <Button className="cancel" label={I18n.t("email.cancel")} onClick={cancel}/>
        <Button label={I18n.t("email.update")} onClick={update}
                disabled={!validEmail(verifiedEmail)}/>
    </div>
</div>
