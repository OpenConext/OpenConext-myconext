<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {me, updateEmail, updateUser} from "../api";
    import {navigate} from "svelte-routing";
    import critical from "../icons/critical.svg";

    const {validEmail} = require("../validation/regexp");
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";

    let verifiedEmail = "";
    let duplicateEmail = false;
    let back = "personal";

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        back = urlSearchParams.get("back") || back;
    })

    const update = () => {
        if (validEmail(verifiedEmail) && verifiedEmail.toLowerCase() !== $user.email.toLowerCase()) {
            updateEmail({...$user, email: verifiedEmail})
                .then(() => {
                    navigate(`/${back}`);
                    flash.setValue(I18n.t("email.updated", {email: verifiedEmail}));
                }).catch(e => {
                if (e.status === 409) {
                    duplicateEmail = true;
                }
            });
        }
    };

    const cancel = () => {
        navigate(`/${back}`);
    }

    $: emailEquality = verifiedEmail.toLowerCase() === $user.email.toLowerCase();

</script>

<style lang="scss">
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

        &.error {
            border: solid 1px var(--color-primary-red);
            background-color: #fff5f3;

            &:focus {
                outline: none;
            }
        }

    }

    div.error {
        display: flex;
        align-items: center;
        margin-top: 10px;

        span.error {
            color: var(--color-primary-red);
        }

        span.svg {
            display: inline-block;
            margin-right: 10px;
        }

    }

    .options {
        margin-top: 60px;
    }

</style>
<div class="email">
    <h2>{I18n.t("email.title")}</h2>
    <p class="info">{I18n.t("email.info")}</p>
    <label for="verifiedEmail">{I18n.t("email.email")}</label>
    <input id="verifiedEmail" class:error={emailEquality || duplicateEmail} type="text" bind:value={verifiedEmail}/>
    {#if emailEquality}
        <div class="error">
            <span class="svg">{@html critical}</span>
            <span class="error">{I18n.t("email.emailEquality")}</span>
        </div>
    {/if}
    {#if duplicateEmail}
        <div class="error">
            <span class="svg">{@html critical}</span>
            <span class="error">{I18n.t("email.duplicateEmail")}</span>
        </div>
    {/if}
    <div class="options">
        <Button className="cancel" label={I18n.t("email.cancel")} onClick={cancel}/>
        <Button label={I18n.t("email.update")} onClick={update}
                disabled={!validEmail(verifiedEmail) || emailEquality}/>
    </div>
</div>
