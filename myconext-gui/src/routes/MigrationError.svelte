<script>
    import I18n from "i18n-js";
    import {flash, duplicatedEmail, user} from "../stores/user";
    import Button from "../components/Button.svelte";
    import {mergeAfterMigration, proceedAfterMigration} from "../api";
    import {navigate} from "svelte-routing";

    const proceed = () => proceedAfterMigration().then(json => {
        $user = {$user, ...json, guest: false};
        navigate("/");
    });

    const migrate = () => mergeAfterMigration().then(json => {
        $user = {$user, ...json, guest: false};
        navigate("/migration");
    });

</script>

<style>
    .migration {
        width: 100%;
        display: flex;
        height: 100%;
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
        color: var(--color-primary-red);
        margin-bottom: 18px;
    }

    p.info {
        margin: 12px 0 16px 0;
    }

    .options {
        margin: 40px;
    }

    @media (max-width: 820px) {
        .left {
            display: none;
        }

        .inner {
            border-left: none;
        }

    }

</style>
<div class="migration">
    <div class="left"></div>
    <div class="inner">
        <h2>{I18n.ts("migrationError.header")}</h2>
        <p class="info">{I18n.t("migrationError.info", {email: $duplicatedEmail} )}</p>
        <p class="question">{I18n.t("migrationError.question")}</p>
        <div class="options">
            <Button className="cancel" href={I18n.t("migrationError.proceed")} label={I18n.t("migrationError.proceed")}
                    onClick={proceed}/>
            <Button href={I18n.t("migrationError.migrate")} label={I18n.t("migrationError.migrate")} onClick={migrate}/>
        </div>
        <div class="help">{@html I18n.t("migrationError.help")}</div>
    </div>

</div>