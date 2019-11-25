<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";

</script>

<style>
    .security {
        width: 100%;
    }

    .inner {
        max-width: 1080px;
        margin: 0 auto;
        padding: 15px 0;
        font-size: 18px;
        display: flex;
        flex-direction: column;
    }

    h2 {
        font-size: 22px;
        margin-bottom: 20px;
    }

    p {
        font-weight: 300;
        margin-bottom: 20px;
    }

    label {
        font-weight: 300;
        margin-right: 20px;
        width: 120px;
    }

    input[type=text], input[type=password] {
        border: 1px solid #dadce0;
        border-radius: 4px;
        font-weight: 300;
        padding: 6px;
        margin: 5px 0;
        font-size: 18px;
        flex-grow: 2;
    }

    div.form-field {
        display: flex;
        width: 100%;
        align-items: center;
        margin-bottom: 15px;
        position: relative;
    }

    span.more {
        position: absolute;
        right: 10px;
        font-size: 32px;
        cursor: pointer;
        color: #007bff;
    }

    @media (max-width: 720px) {
        div.form-field {
            flex-direction: column;
            align-items: flex-start;
        }
        span.more {
            position: unset;
        }
    }


</style>
<div class="security">
    <div class="inner">
        <h2>{I18n.t("security.title")}</h2>
        <p>{I18n.t("security.subTitle")}</p>
        <div class="form-field">
            <label for="email">{I18n.t("security.useMagicLink")}</label>
            <input id="email" type="text" value={$user.email} disabled>
        </div>
        <div class="form-field">
            <label for="name">{I18n.t("security.usePassword")}</label>
            {#if $user.usePassword}
                <input id="name" type="password" value="123456789012345" disabled>
            {:else}
                <input id="name" type="text" value={I18n.t("security.notSet")} disabled>
            {/if}
            <span class="more" on:click={ () => navigate("/password")}>→</span>
        </div>

    </div>

</div>