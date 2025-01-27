<script>
  import Tooltip from './ToolTip.svelte';
  import question from "../icons/question.svg?raw";
  import I18n from "../locale/I18n";

</script>

<style>
    .login-options {
        margin-top: 15px;
        display: flex;
        align-content: center;
        align-items: center;
    }

    .login-options span {
        display: block;
        margin-right: 5px;
    }

    .wrapper {
        color: #0062b0;
    }


</style>

<div class="login-options">
    <span>{I18n.t("Login.LoginOptions.COPY")}</span>
    <div class="wrapper">
        <Tooltip tip={I18n.t("Login.LoginOptionsToolTip.COPY")}>
            {@html question}
        </Tooltip>
    </div>
</div>
