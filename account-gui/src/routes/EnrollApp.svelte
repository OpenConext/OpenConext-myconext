<script>
    import I18n from "i18n-js";
    import {pollEnrollment, startEnrollment} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import ImageContainer from "../components/ImageContainer.svelte";
    import {enrollmentStatus} from "../constants/enrollmentStatus";
    import {poll} from "../utils/poll";
    import {navigate} from "svelte-routing";

    let showSpinner = true;
    let hash = null;
    let qrcode = "";
    let url = "";
    let enrollmentKey = "";
    let onMobile = "ontouchstart" in document.documentElement;
    let status = "NOPE";

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        hash = urlSearchParams.get("h")
        startEnrollment(hash).then(res => {
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
                interval: 500,
                maxAttempts: 2 * 60 * 15 // 15 minute timeout
            }).then(() => navigate("/recovery"))
                .catch(err => {
                    debugger
                });
        });
    });

</script>

<style lang="scss">

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
{#if status === enrollmentStatus.INITIALIZED}
    <h2 class="header">{I18n.t("useApp.scan")}</h2>
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
