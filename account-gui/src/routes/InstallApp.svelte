<script>
    import I18n from "../locale/I18n";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";

    onMount(() => {
        const url = detectMobileOS();
        if (url.startsWith("http")) {
            window.location.href = url;
        } else {
            navigate(url);
        }
    })

    const detectMobileOS = () => {
        const userAgent = navigator.userAgent || navigator.vendor || window.opera;
        if (/iPad|iPhone|iPod/.test(userAgent) && !window.MSStream) {
            return I18n.t("GetApp.Apple.COPY");
        }
        if (/android/i.test(userAgent)) {
            return I18n.t("GetApp.Google.COPY");
        }
        return `${window.location.origin}/getapp`
    }

</script>
<style lang="scss">
    .install-app {
        display: none;
    }
</style>
<div class="install-app">
</div>