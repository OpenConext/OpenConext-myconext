<script>
    import I18n from "../../locale/I18n";
    import Button from "../../components/Button.svelte";
    import appStore from "../../icons/redesign/Download_on_the_App_Store_Badge.svg?raw";
    import googlePlay from "../../icons/redesign/Google_Play-Badge-Logo.wine.svg?raw";
    import QrCode from "svelte-qrcode";
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

    .store-container {
        display: flex;
        gap: 25px;
    }

    .store-icons {
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .qr-container {
        @media (max-width: 820px) {
            display: none;
        }
    }

    :global(.qr-container img) {
        margin-top: 14px;
    }

    :global(.store-icons a.google svg) {
        width: auto;
        height: 69px;
        margin-left: -23px;
    }

    :global(.store-icons a.apple svg) {
        width: 158px;
        height: auto;
    }


</style>
<div class="get-app">
    <div class="inner-container">

        <h2 class="header">{I18n.t("GetApp.Header.COPY")}</h2>
        <p class="explanation">{@html I18n.t("GetApp.Info.COPY")}</p>
        <div class="store-container">
            <div class="qr-container">
                <QrCode size={140}
                        value={`${window.location.origin}/install-app`}
                />
            </div>
            <div class="store-icons">
                <a class="google" href={I18n.t("GetApp.Google.COPY")} target="_blank">
                    {@html googlePlay}
                </a>
                <a class="apple" href={I18n.t("GetApp.Apple.COPY")} target="_blank">
                    {@html appStore}
                </a>
            </div>
        </div>
        <p class="explanation">{@html I18n.t("GetApp.After.COPY")}</p>
        <Button href={I18n.t("GetApp.Next.COPY")} onClick={next} label={I18n.t("GetApp.Next.COPY")} medium={true}/>
    </div>
</div>

