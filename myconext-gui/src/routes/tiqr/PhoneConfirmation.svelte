<script>
    import I18n from "i18n-js";
    import {validatePhoneCode} from "../../api";
    import CodeVerifier from "./CodeVerifier.svelte";
</script>

<style lang="scss">

    .phone-confirmation {
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


</style>
<div class="phone-confirmation">
    <div class="inner-container">
        <h2 class="header">{I18n.t("sms.header")}</h2>
        <p class="explanation">{I18n.t("sms.info")}</p>
        <CodeVerifier navigateTo="/congrats"
                      action={validatePhoneCode}
                      phoneVerificationURL="/phone-verification"/>
    </div>
</div>