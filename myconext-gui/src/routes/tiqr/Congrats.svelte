<script>
    import I18n from "../../locale/I18n";
    import ImageContainer from "../../components/ImageContainer.svelte";
    import icon from "../../icons/redesign/undraw_Order_confirmed_re_g0if.svg?raw";
    import Button from "../../components/Button.svelte";
    import {config} from "../../stores/user";
    import {finishEnrollment} from "../../api";

    let loading = false;

    const nextStep = () => {
        loading = true;
        finishEnrollment().then(res => {
            //need to set secure cookie in login domain
            window.location.href = `${$config.idpBaseUrl}/register/${res.enrollmentVerificationKey}`;
        })
    }

</script>

<style lang="scss">
    .congrats {
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

</style>
<div class="congrats">
    <div class="inner-container">
        <h2 class="header">{I18n.t("congrats.header")}</h2>
        <p class="explanation">{I18n.t("congrats.info")}</p>
        <ImageContainer icon={icon} margin={true}/>
        <Button href={"/next"}
                onClick={nextStep}
                large={true}
                disabled={loading}
                label={I18n.t("congrats.next")}/>
    </div>
</div>