<script>
    import {user} from "../stores/user";
    import I18n from "i18n-js";


</script>

<style>
    .settings {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
    }
</style>
<div class="settings">
    {JSON.stringify($user)}
</div>