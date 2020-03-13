<script>
    import {config} from "../stores/config";
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";

    const register = () => window.location.href = `${$config.eduIDRegisterUrl}?lang=${I18n.locale}`;


</script>

<style>
    .public {
        display: flex;
        flex-direction: column;
        background-color: white;
        height: auto;
        min-height: 500px;
        padding: 25px;
    }

    .container {
        display: flex;
        flex-direction: row;
        width: 100%;
    }

    h1 {
        margin: 15px 0 44px 0;
        font-size: 46px;
        font-weight: 500;
        font-stretch: normal;
        font-style: normal;
        line-height: 1.08;
        letter-spacing: -0.42px;
    }
    div.left {
        width: 60%;
    }

    div.right {
        display: flex;
        flex-direction: column;
        margin: 0 40px 0 65px;
        align-items: center;
        align-content: center;
    }

    div.inner {
        padding: 20px 60px 0 30px;
        border-radius: 8px;
        background-color: #e9f2f7;
        height: 240px;
    }

    h3 {
        font-size: 22px;
        font-weight: 600;
        margin-bottom: 18px;
    }

    div.right p {
        margin-bottom: 42px;
    }

    :global(p) {
        font-size: 18px;
        line-height: 1.33;
        letter-spacing: normal;
    }

    :global(p.divider) {
        margin-bottom: 24px;
    }

    @media (max-width: 800px) {
        div.container {
            flex-direction: column;
        }
        div.left {
            width: 100%;
        }

        div.right {
            margin: 0;
            align-items: normal;
            align-content: flex-start;
        }

    }

</style>


<div class="public">
    <h1>{I18n.t("public.header")}</h1>
    <div class="container">
        <div class="left">
            {@html I18n.t("public.info")}
        </div>
        <div class="right">
            <div class="inner">
                <h3>{I18n.t("public.request.header")}</h3>
                <p>{I18n.t("public.request.info")}</p>
                <Button label={I18n.t("public.request.request")} href="/eduID" onClick={register}/>
            </div>
        </div>
    </div>
</div>
