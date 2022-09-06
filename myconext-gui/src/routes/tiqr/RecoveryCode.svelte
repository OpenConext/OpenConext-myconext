<script>
    import I18n from "i18n-js";
    import Button from "../../components/Button.svelte";
    import {onMount} from "svelte";
    import {generateBackupCode} from "../../api";
    import Spinner from "../../components/Spinner.svelte";
    import {navigate} from "svelte-routing";

    let showSpinner = true;
    let recoveryCode = "";
    let redirect;
    let error;
    let copied = false;

    const onConfirmRefresh = e => {
        e.preventDefault();
        window.removeEventListener("beforeunload", onConfirmRefresh, {capture: true});
        return e.returnValue = I18n.t("recovery.leaveConfirmation");
    }

    onMount(() => {
        generateBackupCode()
            .then(res => {
                const recoveryCodeRaw = res.recoveryCode;
                recoveryCode = recoveryCodeRaw.substring(0, 4) + " " + recoveryCodeRaw.substring(4);
                redirect = res.redirect;
                showSpinner = false;
                window.addEventListener("beforeunload", onConfirmRefresh, {capture: true});
            }).catch(() => {
            showSpinner = false;
            recoveryCode = "";
            error = true;
        })
    });

    const copyToClipboard = () => {
        navigator.clipboard.writeText(recoveryCode);
        copied = true;
        setTimeout(() => copied = false, 1150);
    }

    const next = () => {
        window.removeEventListener("beforeunload", onConfirmRefresh, {capture: true});
        navigate(`/congrats`)
    }

</script>

<style lang="scss">
    .recovery {
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

    div.recovery-code {
        display: flex;
        width: 100%;
        padding: 20px 0;
        margin: 10px 0 25px 0;
        border-top: 1px solid #979797;
        border-bottom: 1px solid #979797;

        span {
            margin: auto;
            font-size: 18px;
            font-weight: bold;
        }

    }

    div.options {
        display: flex;
    }


</style>
{#if showSpinner}
    <Spinner/>
{/if}
<div class="recovery">
    <div class="inner-container">
        <h2 class="header">{I18n.t("recovery.save")}</h2>
        <p class="explanation">{I18n.t("recovery.active")}</p>
        <div class="recovery-code">
            <span>{recoveryCode}</span>
        </div>
        <div class="options">
            <Button onClick={copyToClipboard}
                    href={"/copy"}
                    larger={true}
                    label={copied ? I18n.t("recovery.copied") : I18n.t("recovery.copy")}/>
            <Button onClick={next}
                    larger={true}
                    disabled={error}
                    href={"/next"}
                    label={I18n.t("recovery.continue")}/>
        </div>
    </div>
</div>