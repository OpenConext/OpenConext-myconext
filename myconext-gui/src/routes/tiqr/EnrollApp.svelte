<script>
    import I18n from "i18n-js";
    import {pollEnrollment, startEnrollment} from "../../api/index";
    import Spinner from "../../components/Spinner.svelte";
    import {onMount} from "svelte";
    import ImageContainer from "../../components/ImageContainer.svelte";
    import {enrollmentStatus} from "../../constants/enrollmentStatus";
    import {poll} from "../../utils/poll";
    import {navigate} from "svelte-routing";

    let qrcode = "";
    let url = "";
    let enrollmentKey = "";
    let onMobile = "ontouchstart" in document.documentElement;
    let status = "NOPE";
    let showSpinner = true;
    let timeOut = false;

    onMount(() => {
        startEnrollment().then(res => {
            qrcode = res.qrcode;
            url = res.url;
            enrollmentKey = res.enrollmentKey;
            showSpinner = false;
            status = enrollmentStatus.INITIALIZED;
            poll({
                fn: () => pollEnrollment(enrollmentKey),
                validate: currentStatus => {
                    if (currentStatus === enrollmentStatus.RETRIEVED) {
                        status = currentStatus;
                    }
                    return currentStatus === enrollmentStatus.PROCESSED
                },
                interval: 1000,
                maxAttempts: 60 * 15 // 15 minute timeout
            }).then(() => navigate(`/recovery`))
                .catch(() => {
                    timeOut = true;
                });
        });
    });

</script>

<style lang="scss">

    .enroll-app {
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

    img.qr-code {
        cursor: none;
    }

    div.spinner-container {
        margin: 80px 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        align-content: center;
    }


</style>

{#if showSpinner}
    <Spinner/>
{/if}
<div class="enroll-app">
    <div class="inner-container">

        {#if status === enrollmentStatus.INITIALIZED}
            <h2 class="header">{I18n.t("enrollApp.scan")}</h2>
            <ImageContainer>
                {#if onMobile}
                    <a href={url}><img class="qr-code" src="{qrcode}" alt="qr-code"></a>
                {:else}
                    <img class="qr-code" src="{qrcode}" alt="qr-code">
                {/if}
            </ImageContainer>
        {:else if status === enrollmentStatus.RETRIEVED}
            <h2 class="header">{I18n.t("enrollApp.header")}</h2>
            <div class="spinner-container">
                <Spinner relative={true}/>
            </div>
        {/if}
    </div>
</div>
