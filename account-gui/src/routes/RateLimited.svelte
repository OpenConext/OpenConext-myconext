<script>
    import I18n from "../locale/I18n";
    import {user} from "../stores/user";
</script>

<style lang="scss">

    div.login-code {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    p.info {
        margin-bottom: 40px;
    }

    h2.header {
        margin: 6px 0 30px 0;
        text-align: center;
        color: var(--color-primary-red);
        font-size: 28px;
    }


</style>
<div class="login-code">
    <h2 class="header">{I18n.t("LoginCode.ErrorHeader.COPY")}</h2>
    <p class="info">{@html I18n.t("LoginCode.ErrorInfo.COPY", {email: $user.email})}</p>
</div>
