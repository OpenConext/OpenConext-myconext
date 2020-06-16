<script>
    import I18n from "i18n-js";
    import {flash, duplicatedEmail, user} from "../stores/user";
    import Button from "../components/Button.svelte";
    import {mergeAfterMigration, proceedAfterMigration} from "../api";
    import {navigate} from "svelte-routing";

    const proceed = () => proceedAfterMigration().then(json => {
        for (var key in json) {
            if (json.hasOwnProperty(key)) {
                $user[key] = json[key];
            }
        }
        $user.guest = false;
        navigate("/");
    });

    const migrate = () => mergeAfterMigration().then(json => {
        for (var key in json) {
            if (json.hasOwnProperty(key)) {
                $user[key] = json[key];
            }
        }
        $user.guest = false;
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

    ol.ordered-list {
        list-style: decimal;
        margin-top: 15px;
        margin-left: 40px;
        line-height: 24px;
    }

    li.last-child {
        margin-top: 15px;
    }

    ul.non-ordered-list {
        list-style: initial;
        margin-left: 40px;
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
        .options {
            margin: 40px 10px;
        }

    }

</style>
<div class="migration">
    <div class="left"></div>
    <div class="inner">
        <h2>{I18n.t("migrationError.header")}</h2>
        <p class="info">{I18n.t("migrationError.info", {email: $duplicatedEmail} )}</p>
        <ol class="ordered-list">
            <li>
                <span>{I18n.t("migrationError.sub1")}</span>
                <ul class="non-ordered-list">
                    <li>{I18n.t("migrationError.sub1Inner1")}</li>
                    <li>{I18n.t("migrationError.sub1Inner2")}</li>
                </ul>
            </li>
            <li class="last-child">
                <span>{I18n.t("migrationError.sub2")}</span>
                <ul class="non-ordered-list">
                    <li>{I18n.t("migrationError.sub2Inner1")}</li>
                    <li>{I18n.t("migrationError.sub2Inner2")}</li>
                </ul>
            </li>
        </ol>
        <div class="options">
            <Button className="cancel" href={I18n.t("migrationError.abort")} label={I18n.t("migrationError.abortMigration")}
                    onClick={proceed}/>
            <Button href={I18n.t("migrationError.continue")} label={I18n.t("migrationError.continueMigration")} onClick={migrate}/>
        </div>
        <div class="help">{@html I18n.t("migrationError.help")}</div>
    </div>

</div>