<script>
    import I18n from "../../locale/I18n";
    import Button from "../../components/Button.svelte";
    import appStore from "../../icons/redesign/Download_on_the_App_Store_Badge.svg?raw";
    import googlePlay from "../../icons/redesign/Google_Play-Badge-Logo.wine.svg?raw";
    import {navigate} from "svelte-routing";

    const next = () => {
        navigate(`/enroll-app`)
    };

</script>

<style lang="scss">
    .get-app {
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

    .store-icons {
        display: flex;

        @media (max-width: 820px) {
            flex-direction: column;
        }
    }

    :global(.store-icons a.apple svg) {
        width: 158px;
        height: auto;
        margin-top: 11px;
        margin-left: 3px;
    }

    :global(.store-icons a.google svg) {
        width: auto;
        height: 68px;
        margin-left: -23px;
    }

</style>
<div class="get-app">
    <div class="inner-container">

        <h2 class="header">{I18n.t("GetApp.Header.COPY")}</h2>
        <p class="explanation">{@html I18n.t("GetApp.Info.COPY")}</p>
        <div class="store-icons">
            <a class="google" href={I18n.t("GetApp.Google.COPY")} target="_blank">
                {@html googlePlay}
            </a>
            <a class="apple" href={I18n.t("GetApp.Apple.COPY")} target="_blank">
                {@html appStore}
            </a>
        </div>
        <p class="explanation">{@html I18n.t("GetApp.After.COPY")}</p>
        <Button href={I18n.t("GetApp.Next.COPY")} onClick={next} label={I18n.t("GetApp.Next.COPY")} medium={true}/>
    </div>
</div>

