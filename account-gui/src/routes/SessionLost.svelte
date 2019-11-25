<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";

</script>

<style>
    .home {
        display: flex;
        justify-content: center;
        width: 100%;
    }

    .card {
        display: flex;
        flex-direction: column;
        padding: 50px;
        background-color: white;
        min-height: 500px;
        width: 500px;
        color: #767676;
    }
    h2 {
        margin-bottom: 40px;
    }

</style>
<div class="home">
    <div class="card">
        <h2>{I18n.ts("session.title")}</h2>
        <p>{@html I18n.ts("session.info")}</p>
    </div>
</div>