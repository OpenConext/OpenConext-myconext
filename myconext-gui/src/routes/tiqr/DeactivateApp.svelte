<script>
    import {user} from "../../stores/user";
    import I18n from "../../locale/I18n";
    import {navigate} from "svelte-routing";
    import Button from "../../components/Button.svelte";
    import {onMount, tick} from "svelte";
    import {deactivateApp, me, sendDeactivationPhoneCode} from "../../api";
    import Spinner from "../../components/Spinner.svelte";
    import critical from "../../icons/critical.svg?raw";
    import CodeVerifier from "./CodeVerifier.svelte";

    let useRecoveryCode;
    let recoveryCode = "";
    let recoveryCodeRef = null;
    let showSpinner = false;
    let step = 1;
    let wrongCode = false;
    let validTotp = false;
    let maxAttempts = false;

    const onValid = () => {
        validTotp = true;
    }

    onMount(() => {
        useRecoveryCode = $user.registration.recoveryCode;
    })

    const nextStep = () => {
        if (!useRecoveryCode) {
            showSpinner = true;
            sendDeactivationPhoneCode()
                .then(() => {
                    showSpinner = false;
                    step = 2;
                });
        } else {
            step = 2;
        }

    }

    const deactivateUserAction = () => {
        showSpinner = true;
        deactivateApp(recoveryCode)
            .then(() => {
                me().then(json => {
                    for (var key in json) {
                        if (json.hasOwnProperty(key)) {
                            $user[key] = json[key];
                        }
                    }
                    navigate("/security");
                });
            }).catch(e => {
            recoveryCode = "";
            showSpinner = false;
            wrongCode = true;
            tick().then(() => recoveryCodeRef.focus());
            if (e.status === 429) {
                maxAttempts = true;
            }
        });
    }

</script>

<style lang="scss">
    .deactivate-app {
        width: 100%;
        height: 100%;
    }

    .inner-container {
        height: 100%;
        margin: 0 auto;
        padding: 15px 30px 15px 0;
        display: flex;
        flex-direction: column;
    }

    h2 {
        margin: 20px 0 10px 0;
        color: var(--color-primary-green);
    }

    p.explanation {
        margin: 15px 0;
    }

    label {
        font-weight: 600;
        margin: 33px 0 13px 0;
        display: inline-block;
    }

    input {
        border-radius: 8px;
        border: solid 1px #676767;
        padding: 14px;
        font-size: 16px;
    }

    div.error {
        display: flex;
        align-items: center;
        color: var(--color-primary-red);
        margin: 25px 0;
    }

    div.error span.svg {
        display: inline-block;
        margin-right: 10px;
    }

    .options {
        margin-top: 40px;
        display: flex;
    }


</style>
{#if showSpinner}
    <Spinner/>
{/if}

<div class="deactivate-app">
    <div class="inner-container">

        <h2>{I18n.t("Deactivate.TitleDelete.COPY")}</h2>
        <p class="explanation">{I18n.t("Deactivate.Info.COPY")}</p>
        {#if !useRecoveryCode && step === 1}
            <p class="explanation">{I18n.t("Deactivate.SendSms.COPY")}</p>
        {:else if useRecoveryCode}
            <p class="explanation">{@html I18n.t("Deactivate.RecoveryCodeInfo.COPY")}</p>
        {/if}
        {#if useRecoveryCode}
            <input id="recoveryCode" type="text" spellcheck="false" bind:value={recoveryCode}/>
            {#if wrongCode && !maxAttempts}
                <div class="error">
                    <span class="svg">{@html critical}</span>
                    <span>{I18n.t("Deactivate.CodeIncorrect.COPY")}</span>
                </div>
            {/if}
            {#if maxAttempts}
                <div class="error">
                    <span class="svg">{@html critical}</span>
                    <span>{@html I18n.t("Deactivate.MaxAttempts.COPY")}</span>
                </div>
            {/if}
        {:else if step === 2 && !useRecoveryCode}
            <label for="recoveryCode">{I18n.t("Deactivate.VerificationCode.COPY")}</label>
            <CodeVerifier navigateTo="/security"
                          action={deactivateApp}
                          reEnter={false}
                          onValid={onValid}/>
        {/if}
        <div class="options">
            <Button href="/cancel"
                    label={I18n.t("YourVerifiedInformation.ConfirmRemoval.Button.Cancel.COPY")}
                    onClick={() => navigate("/security")}
                    medium={true}
                    className="cancel"/>
            <Button href="/deactivate"
                    label={I18n.t(`deactivate.${(step === 1 && !useRecoveryCode)? "next" : "deactivateApp" }`)}
                    medium={true}
                    disabled={(recoveryCode === "" && useRecoveryCode) || (step === 2 && !validTotp) || (maxAttempts)}
                    onClick={() => (step === 1 && !useRecoveryCode) ? nextStep() : deactivateUserAction()}/>
        </div>
    </div>
</div>