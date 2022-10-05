<script>
    import {user, config, redirectPath} from "../stores/user";
    import {onMount} from "svelte";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";

    let isLogoutRedirect = false;
    let isAccountDeletionRedirect = false;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        isLogoutRedirect = urlSearchParams.get("logout");
        isAccountDeletionRedirect = urlSearchParams.get("delete");
        if (!isLogoutRedirect && !isAccountDeletionRedirect) {
            navigate("/404");
        }
    });

    const loginAgain = () => {
        window.location.href = `${$config.idpBaseUrl}/doLogin`;
    }

</script>

<style>
    .landing {
        display: flex;
        flex-direction: column;
        background-color: white;
        height: auto;
        min-height: 500px;
        align-items: center;
        align-content: center;
    }

    div.inner {
        margin: 25px auto;
    }

    h3 {
        color: var(--color-primary-green);
        margin-bottom: 40px;
    }

</style>


<div class="landing">
    <div class="inner">
        {#if isLogoutRedirect}
            <h3>{I18n.t("landing.logoutTitle")}</h3>
            <div >
                <Button label={I18n.t("landing.loginAgain")}
                        large={true}
                        onClick={loginAgain}/>
            </div>

        {/if}
        {#if isAccountDeletionRedirect}
            <h3>{I18n.t("landing.deleteTitle")}</h3>
            <div >
                <Button label={I18n.t("landing.registerAgain")}
                        large={true}
                        onClick={loginAgain}/>
            </div>
        {/if}
    </div>
</div>
