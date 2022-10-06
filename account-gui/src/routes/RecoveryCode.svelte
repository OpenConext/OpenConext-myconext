<script>
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";
    import {onMount} from "svelte";
    import {links} from "../stores/conf";
    import {generateBackupCode} from "../api";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";

    let showSpinner = true;
    let hash = null;
    let recoveryCode;
    let redirect;
    let error;
    let copied = false;

    const onConfirmRefresh = e => {
        e.preventDefault();
        window.removeEventListener("beforeunload", onConfirmRefresh, {capture: true});
        return e.returnValue = I18n.t("recovery.leaveConfirmation");
    }

    onMount(() => {
        $links.displayBackArrow = false;

        const urlParams = new URLSearchParams(window.location.search);
        hash = urlParams.get("h");
        generateBackupCode(hash).then(res => {
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
            font-weight: bold;
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
<h2 class="header">{I18n.t("recovery.save")}</h2>
<p class="explanation">{I18n.t("recovery.active")}</p>
<div class="recovery-code">
    <span>{recoveryCode}</span>
</div>
<div class="options">
    <Button onClick={copyToClipboard}
            href={"/copy"}
            className="full"
            label={copied ? I18n.t("recovery.copied") : I18n.t("recovery.copy")}/>
    <Button onClick={next}
            className="cancel full"
            disabled={error}
            href={"/next"}
            label={I18n.t("recovery.continue")}/>
</div>
