<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";

    export let id;

</script>

<style>
    h1 {
        font-size: 52px;
        font-weight: bold;
    }

    h2 {
        margin-bottom: 25px;
        font-size: 36px;
        font-weight: bold;
        color: var(--color-primary-green);
    }

    p.info {
        color: #767676;
        margin-bottom: 40px;
    }

    p.mail {
        color: #404040;
        margin-bottom: 60px;
        font-size: 24px;
    }

</style>
<h1>{I18n.ts("magicLink.header")}</h1>
<h2>{I18n.ts("magicLink.header2")}</h2>
<p class="info">{I18n.ts("magicLink.info")}</p>
<p class="mail">{$user.email}</p>
<a href="/login/{id}">{I18n.ts("magicLink.wrongEmail")}</a>
