<script>
    import I18n from "../../locale/I18n";
    import ImageContainer from "../../components/ImageContainer.svelte";
    import icon from "../../icons/redesign/undraw_Order_confirmed_re_g0if.svg?raw";
    import Button from "../../components/Button.svelte";
    import {navigate} from "svelte-routing";
    import {me} from "../../api";
    import {user} from "../../stores/user";

    let loading = false;

    const nextStep = () => {
        loading = true;
        me().then(json => {
            for (var key in json) {
                if (json.hasOwnProperty(key)) {
                    $user[key] = json[key];
                }
            }
            navigate("/security");
        });
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
        <p class="explanation">{I18n.t("congrats.changeInfo")}</p>
        <ImageContainer icon={icon} margin={true}/>
        <Button href={"/next"}
                onClick={nextStep}
                large={true}
                disabled={loading}
                label={I18n.t("congrats.next")}/>
    </div>
</div>