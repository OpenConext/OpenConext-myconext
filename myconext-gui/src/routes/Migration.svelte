<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {me, updateUser} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";

    const proceed = () => navigate("/");

</script>

<style>
    .migration {
        width: 100%;
        display: flex;
        height: 100%;
    }

    @media (max-width: 820px) {
        .left {
            display: none;
        }

        .inner {
            border-left: none;
        }
    }

    .left {
        background-color: #f3f6f8;
        width: 270px;
        height: 100%;
        border-bottom-left-radius: 8px;
    }

    .inner {
        margin: 20px 0 190px 0;
        padding: 15px 15px 0 40px;
        border-left: 2px solid var(--color-primary-grey);
        display: flex;
        flex-direction: column;
        background-color: white;
    }

    h2 {
        color: var(--color-primary-green);
        margin-bottom: 18px;
    }

    h3 {
        margin-bottom: 28px;
    }

    p.info {
        margin: 12px 0 16px 0;
    }

    .options {
        margin-top: 60px;
    }

    :global(.options a:not(:first-child)) {
        margin-left: 25px;
    }


</style>
<div class="migration">
    <div class="left"></div>
    <div class="inner">
        <h2>{I18n.ts("migration.header")}</h2>
        <h3>{I18n.ts("migration.header2")}</h3>
        <p class="info">{I18n.t("migration.info")}</p>
        <div class="options">
            <Button label={I18n.ts("migration.link")} onClick={proceed}/>
        </div>
    </div>

</div>