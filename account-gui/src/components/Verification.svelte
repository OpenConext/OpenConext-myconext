<script>
    import I18n from "../locale/I18n";
    import oneMoreThingEmpty from "../icons/onemorething_empty.svg?raw";
    import oneMoreThingFilled from "../icons/onemorething_filled.svg?raw";
    import DOMPurify from "dompurify";

    export let explanation;
    export let explanationText;
    export let verified = false;

</script>
<style>
    p.info {
        margin-bottom: 25px;
    }

    div.conditions-container {
        display: flex;
        align-items: center;
        padding: 15px;
        border: 2px solid var(--color-primary-grey);;
        margin-bottom: 25px;
        border-radius: 8px;
    }

    p.conditions {
        margin-right: 25px;
    }

    span.icon {
        display: inline-block;
        margin-left: auto;
    }
</style>
{#if verified}
    <p class="info">{I18n.t("confirmStepup.conditionMet")}</p>
{/if}
<div class="conditions-container">
    <p class="conditions">
        {@html verified ? I18n.t(`stepUpVerification.${explanation}`) : (DOMPurify.sanitize(explanationText) || I18n.t(`stepUpExplanation.${explanation}`))}
    </p>
    <span class="icon">{@html verified ? oneMoreThingFilled : oneMoreThingEmpty}</span>

</div>

