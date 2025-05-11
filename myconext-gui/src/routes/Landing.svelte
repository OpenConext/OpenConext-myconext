<script>
    import {config} from "../stores/user";
    import {onMount} from "svelte";
    import I18n from "../locale/I18n";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";

    let isLogoutRedirect = false;
    let isAccountDeletionRedirect = false;
    let isRateLimitedRedirect = false;

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        isLogoutRedirect = urlSearchParams.get("logout");
        isAccountDeletionRedirect = urlSearchParams.get("delete");
        isRateLimitedRedirect = urlSearchParams.get("ratelimit");
        if (!isLogoutRedirect && !isAccountDeletionRedirect && !isRateLimitedRedirect) {
            navigate("/404");
        }
    });

    const loginAgain = () => {
        window.location.href = `${$config.idpBaseUrl}/doLogin?register=false`;
    }

</script>

<style lang="scss">
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
        margin: 25px auto auto 200px;
        max-width: 600px;

        @media (max-width: 800px) {
            margin: 25px auto;
        }
    }

    h3 {
        color: var(--color-primary-green);
        margin-bottom: 40px;
    }

</style>


<div class="landing">
    <div class="inner">
        {#if isLogoutRedirect}
            <h3>{I18n.t("Landing.LogoutTitle.COPY")}</h3>
            <div>
                <Button label={I18n.t("RegistrationCheck.LoginAgain.COPY")}
                        large={true}
                        onClick={loginAgain}/>
            </div>

        {/if}
        {#if isAccountDeletionRedirect}
            <h3>{I18n.t("RegistrationCheck.DeleteTitle.COPY")}</h3>
            <div>
                <Button label={I18n.t("RegistrationCheck.RegisterAgain.COPY")}
                        large={true}
                        onClick={loginAgain}/>
            </div>
        {/if}
        {#if isRateLimitedRedirect}
            <h3>{I18n.t("Landing.RateLimitTitle.COPY")}</h3>
            <div>
                <Button label={I18n.t("RegistrationCheck.LoginAgain.COPY")}
                        large={true}
                        onClick={loginAgain}/>
            </div>
        {/if}
    </div>
</div>
