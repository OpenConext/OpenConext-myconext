<script>
    import I18n from "../../locale/I18n";
    import {reValidatePhoneCode, validatePhoneCode} from "../../api";
    import CodeVerifier from "./CodeVerifier.svelte";

    export let change = false;

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
        <h2 class="header">{I18n.t("Sms.Header.COPY")}</h2>
        <p class="explanation">{I18n.t("Sms.Info.COPY")}</p>
        <CodeVerifier navigateTo={change ? `/change-congrats` : `/congrats`}
                      action={change ? reValidatePhoneCode : validatePhoneCode}
                      phoneVerificationURL={`/${change ? "change-" : ""}phone-verification`}/>
    </div>
</div>