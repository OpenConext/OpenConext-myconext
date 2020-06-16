<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import chevron_right from "../icons/chevron-right.svg";

</script>

<style>
    .profile {
        width: 100%;
        height: 100%;
    }

    .inner {
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

    p.info {
        font-weight: 300;
        margin-bottom: 26px;
    }

    p.info2 {
        font-size: 22px;
        margin-bottom: 24px;
        font-family: Proxima Nova, sans-serif;
    }

    p {
        font-size: 18px;
        line-height: 1.33;
        letter-spacing: normal;
    }

    table {
        width: 100%;
    }
    tr.name {
        cursor: pointer;
    }
    td {
        border-bottom: 1px solid var(--color-primary-grey);
    }
    td.attr {
        width: 30%;
        padding: 20px;
    }

    td.value {
        width: 70%;
        font-weight: bold;
        padding-left: 20px;
    }

    div.value-container {
        display: flex;
        align-items: center;
    }

    div.value-container a.menu-link{
        margin-left: auto;
    }

    :global(a.menu-link svg) {
        fill: var(--color-primary-green);
    }


</style>
<div class="profile">
    <div class="inner">
        <h2>{I18n.t("profile.title")}</h2>
        <p class="info">{I18n.t("profile.info")}</p>
        <p class="info2">{I18n.t("profile.profile")}</p>
        <table cellspacing="0">
            <thead></thead>
            <tbody>
            <tr>
                <td class="attr">{I18n.t("profile.email")}</td>
                <td class="value">{$user.email}</td>
            </tr>
            <tr class="name" on:click={() => navigate("/edit")}>
                <td class="attr">{I18n.t("profile.name")}</td>
                <td class="value">
                    <div class="value-container">
                    <span>{`${$user.givenName} ${$user.familyName}`}</span>
                    <a class="menu-link" href="/name"
                       on:click|preventDefault|stopPropagation={() => navigate("/edit")}>{@html chevron_right}</a>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

</div>