<script>
    import I18n from "../../locale/I18n";
    import Button from "../../components/Button.svelte";
    import {onDestroy, onMount} from "svelte";
    import {generateBackupCode, regenerateBackupCode} from "../../api";
    import Spinner from "../../components/Spinner.svelte";
    import {navigate} from "svelte-routing";

    export let change = false;

    const RECOVERY_CODE = "recovery-code";

    let showSpinner = true;
    let recoveryCode = "";
    let copied = false;

    const onConfirmRefresh = e => {
        e.preventDefault();
        window.removeEventListener("beforeunload", onConfirmRefresh, {capture: true});
        return e.returnValue = I18n.t("Recovery.LeaveConfirmation.COPY");
    }

    onMount(() => {
        const promise = change ? regenerateBackupCode : generateBackupCode;
        promise()
            .then(res => {
                const recoveryCodeRaw = res.recoveryCode;
                recoveryCode = recoveryCodeRaw.substring(0, 4) + " " + recoveryCodeRaw.substring(4);
                sessionStorage.setItem(RECOVERY_CODE, recoveryCode);
                showSpinner = false;
                window.addEventListener("beforeunload", onConfirmRefresh, {capture: true});
            }).catch(() => {
            showSpinner = false;
            recoveryCode = sessionStorage.getItem(RECOVERY_CODE);
        })
    });

    onDestroy(() => {
        window.removeEventListener("beforeunload", onConfirmRefresh, {capture: true});
    });

    const copyToClipboard = () => {
        navigator.clipboard.writeText(recoveryCode);
        copied = true;
        setTimeout(() => copied = false, 1150);
    }

    const next = () => {
        window.removeEventListener("beforeunload", onConfirmRefresh, {capture: true});
        navigate(change ? `/change-congrats` : `/congrats`);
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
            font-size: 20px;
            font-weight: 600;
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
        <h2 class="header">{I18n.t("Recovery.Save.COPY")}</h2>
        <p class="explanation">{I18n.t("Recovery.Active.COPY")}</p>
        <div class="recovery-code">
            <span>{recoveryCode}</span>
        </div>
        <div class="options">
            <Button onClick={copyToClipboard}
                    href={"/copy"}
                    larger={true}
                    label={copied ? I18n.t("Recovery.Copied.COPY") : I18n.t("Recovery.Copy.COPY")}/>
            <Button onClick={next}
                    larger={true}
                    href={"/next"}
                    label={I18n.t("Recovery.Continue.COPY")}/>
        </div>
    </div>
</div>