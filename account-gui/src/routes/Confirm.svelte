<script>
    import I18n from "i18n-js";
    import {onMount} from 'svelte';
    import {user} from "../stores/user";

    let email = null;

    onMount(() => {
        if (typeof window !== "undefined") {
            const urlSearchParams = new URLSearchParams(window.location.search);
            email = decodeURIComponent(urlSearchParams.get("email"));
        }
    });


    const proceed = () => {
        if (typeof window !== "undefined") {
            const urlSearchParams = new URLSearchParams(window.location.search);
            const redirect = decodeURIComponent(urlSearchParams.get("redirect"));
            window.location.href = `${redirect}?h=${urlSearchParams.get('h')}`;
        }

    };
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
        background-color: white;
        height: auto;
        min-height: 500px;
        width: 500px;
        padding: 25px;
    }

    .button {
        border: 1px solid #818181;
        width: 100%;
        background-color: #c7c7c7;
        border-radius: 2px;
        padding: 10px 20px;
        display: inline-block;
        color: black;
        text-decoration: none;
        cursor: pointer;
        text-align: center;
    }

    h1 {
        color: #767676;
        margin-bottom: 35px;
    }
    p {
        color: #767676;
        margin-bottom: 35px;
    }

</style>
<div class="home">
    <div class="card">
        <h1>{@html I18n.ts("confirm.header")}</h1>
        <p>{I18n.ts("confirm.thanks")}</p>
        <p>{I18n.ts("confirm.info")}</p>
        <a href="/proceed" class="button"
           on:click|preventDefault|stopPropagation={proceed}>{I18n.ts("confirm.link")}</a>
    </div>
</div>