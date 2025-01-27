<script>
    import I18n from "../../locale/I18n";
    import {pollEnrollment, startEnrollment} from "../../api/index";
    import Spinner from "../../components/Spinner.svelte";
    import {onDestroy, onMount} from "svelte";
    import ImageContainer from "../../components/ImageContainer.svelte";
    import {enrollmentStatus} from "../../constants/enrollmentStatus";
    import {poll} from "../../utils/poll";
    import {navigate} from "svelte-routing";
    import Button from "../../components/Button.svelte";

    let qrcode = "";
    let url = "";
    let enrollmentKey = "";
    let onMobile = "ontouchstart" in document.documentElement;
    let status = "NOPE";
    let showSpinner = true;
    let timeOut = false;
    let existingRegistration = false;

    onDestroy(() => timeOut = true);

    onMount(() => {
        startEnrollment()
            .then(res => {
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
                        return currentStatus === enrollmentStatus.PROCESSED || timeOut;
                    },
                    interval: 1000,
                    maxAttempts: 60 * 15 // 15 minute timeout
                })
                    .then(() => !timeOut && navigate(`/recovery`))
                    .catch(() => {
                        timeOut = true;
                    });
            })
            .catch(() => {
                existingRegistration = true;
                showSpinner = false;
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

    .mobile-qr-code {
        display: flex;
        flex-direction: column;

        .button-link-container {
            margin: auto;
        }
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
        {#if existingRegistration}
            <h2 class="header">{I18n.t("enrollApp.existingRegistration")}</h2>
            <p class="time-out">
                <span>{I18n.t("enrollApp.existingRegistrationInfoFirst")}</span>
                <a href="/security">
                    {I18n.t("UseApp.TimeOutInfoLink.COPY")}
                </a>
                <span>{I18n.t("enrollApp.existingRegistrationInfoLast")}</span>
            </p>
        {:else if timeOut}
            <h2 class="header">{I18n.t("UseApp.TimeOut.COPY")}</h2>
            <p class="time-out">
                <span>{I18n.t("UseApp.TimeOutInfoFirst.COPY")}</span>
                <a href="/"
                   on:click|preventDefault|stopPropagation={() => window.location.reload(true)}>
                    {I18n.t("UseApp.TimeOutInfoLink.COPY")}
                </a>
                <span>{I18n.t("UseApp.TimeOutInfoLast.COPY")}</span>
            </p>

        {:else if status === enrollmentStatus.INITIALIZED}
            <h2 class="header">{I18n.t("UseApp.Scan.COPY")}</h2>
            <ImageContainer>
                {#if onMobile}
                    <div class="mobile-qr-code">
                        <a class="qr-code-link" href={url}>
                            <img class="qr-code" src="{qrcode}" alt="qr-code">
                        </a>
                        <div class="button-link-container">
                            <Button href={url}
                                    onClick={() => window.location.href = url}
                                    larger={true}
                                    label={I18n.t("UseApp.OpenEduIDApp.COPY")}/>
                        </div>
                    </div>
                {:else}
                    <img class="qr-code" src="{qrcode}" alt="qr-code">
                {/if}
            </ImageContainer>
        {:else if status === enrollmentStatus.RETRIEVED}
            <h2 class="header">{I18n.t("EnrollApp.Header.COPY")}</h2>
            <div class="spinner-container">
                <Spinner relative={true}/>
            </div>
        {/if}
    </div>
</div>
