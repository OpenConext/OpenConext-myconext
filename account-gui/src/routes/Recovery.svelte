<script>
    import {links} from "../stores/conf";
    import I18n from "../locale/I18n";
    import {onMount} from "svelte";
    import phoneIcon from "../icons/redesign/mobile-phone.svg";
    import backupIcon from "../icons/redesign/printer.svg";
    import LoginOption from "../components/LoginOption.svelte";
    import {navigate} from "svelte-routing";

    let hash = null;
    let showSpinner = true;


    onMount(() => {
        $links.displayBackArrow = false;

        const urlParams = new URLSearchParams(window.location.search);
        hash = urlParams.get("h");
    });

    const phoneNumber = () => {
        navigate(`/phone-verification?h=${hash}`);
    }

    const backUpCode = () => {
        navigate(`/recovery-code?h=${hash}`);
    }

</script>

<style lang="scss">

    p.explanation {
        margin: 15px 0;
    }

    p.methods {
        margin-bottom: 15px;
    }
</style>
<h2 class="header">{I18n.t("recovery.header")}</h2>
<p class="explanation">{I18n.t("recovery.info")}</p>
<p class="methods">{I18n.t("recovery.methods")}</p>
<div class="phone-number">
    <LoginOption icon={phoneIcon}
                 label={I18n.t("recovery.phoneNumber")}
                 subLabel={I18n.t("recovery.phoneNumberInfo")}
                 action={phoneNumber}
                 index={1}
                 preferred={true}/>
</div>
<div class="other-account">
    <LoginOption icon={backupIcon}
                 label={I18n.t("recovery.backupCode")}
                 subLabel={I18n.t("recovery.backupCodeInfo")}
                 action={backUpCode}
                 index={2}/>
</div>
