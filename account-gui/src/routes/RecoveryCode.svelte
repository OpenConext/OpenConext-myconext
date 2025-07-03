<script>
    import I18n from "../locale/I18n";
    import Button from "../components/Button.svelte";
    import {onDestroy, onMount} from "svelte";
    import {links} from "../stores/conf";
    import {generateBackupCode} from "../api";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";

    const RECOVERY_CODE = "recovery-code";
    const REDIRECT = "redirect";

    let showSpinner = true;
    let hash = null;
    let recoveryCode;
    let redirect;
    let copied = false;

    const onConfirmRefresh = e => {
        e.preventDefault();
        window.removeEventListener("beforeunload", onConfirmRefresh, {capture: true});
        return e.returnValue = I18n.t("Recovery.LeaveConfirmation.COPY");
    }

    onMount(() => {
        $links.displayBackArrow = false;

        const urlParams = new URLSearchParams(window.location.search);
        hash = urlParams.get("h");

        window.addEventListener("beforeunload", onConfirmRefresh, {capture: true});

        generateBackupCode(hash).then(res => {
            const recoveryCodeRaw = res.recoveryCode;
            recoveryCode = recoveryCodeRaw.substring(0, 4) + " " + recoveryCodeRaw.substring(4);
            redirect = res.redirect;
            sessionStorage.setItem(RECOVERY_CODE, recoveryCode);
            sessionStorage.setItem(REDIRECT, redirect);
            showSpinner = false;
        }).catch(() => {
            showSpinner = false;
            recoveryCode = sessionStorage.getItem(RECOVERY_CODE);
            redirect = sessionStorage.getItem(REDIRECT);
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
        navigate(`/congrats?h=${hash}&redirect=${encodeURIComponent(redirect)}`)
    }

</script>

<style lang="scss">
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
            font-weight: 600;
        }

    }

    div.options {
        display: flex;
        flex-direction: column;
    }

</style>
{#if showSpinner}
    <Spinner/>
{/if}
<h2 class="header">{I18n.t("Recovery.Save.COPY")}</h2>
<p class="explanation">{I18n.t("Recovery.Active.COPY")}</p>
<div class="recovery-code">
    <span>{recoveryCode}</span>
</div>
<div class="options">
    <Button onClick={copyToClipboard}
            href={"/copy"}
            className="full"
            label={copied ? I18n.t("Recovery.Copied.COPY") : I18n.t("Recovery.Copy.COPY")}/>
    <Button onClick={next}
            className="cancel full"
            href={"/next"}
            label={I18n.t("Recovery.Continue.COPY")}/>
</div>
