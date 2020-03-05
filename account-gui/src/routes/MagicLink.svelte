<script>
    import I18n from "i18n-js";
    import {user} from "../stores/user";
    import {onMount} from "svelte";
    import info from "../icons/icons8-info.svg";

    export let id;
    let serviceName;
    let modus;

    onMount(() => {
        const urlParams = new URLSearchParams(window.location.search);
        serviceName = urlParams.get("name");
        modus = urlParams.get("modus");
    });

</script>

<style>
    h2.header {
        font-weight: 300;
    }

    h2.top {
        margin: 6px 0 30px 0;
        color: var(--color-primary-green);
        font-size: 28px;
    }

    p.mail {
        font-weight: bold;
    }

    div.whoops {
        margin: 40px 0;
        display: flex;
        padding: 25px;
        border-radius: 8px;
        background-color: #efefef;
        font-size: 14px;
        align-items: center;
    }
    div.whoops .info {
        margin-right: 20px;
    }

    div.whoops a {
        text-decoration: underline;
    }

</style>
<h2 class="header">{I18n.ts("magicLink.header")}</h2>
<h2 class="top">{I18n.ts("magicLink.header2")}</h2>
<p>{I18n.ts("magicLink.info1")}</p>
<p class="mail">{$user.email}</p>
<p>{I18n.ts("magicLink.info2")}</p>
<div class="whoops">
    <div class="info">{@html info}</div>
    <div>
        <p>{I18n.ts("magicLink.wrongEmail")}</p>
        <a href={`/login/${id}?name=${serviceName}&modus=${modus}`}>{I18n.ts("magicLink.wrongEmail2")}</a>
    </div>
</div>

