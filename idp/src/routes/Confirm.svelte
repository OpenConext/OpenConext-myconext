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
        border-radius: 4px;
        background-color: white;
        height: auto;
        min-height: 500px;
        width: auto;
        min-width: 500px;
        align-items: center;
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

    h3 {
        color: #767676;
        margin-bottom: 35px;
    }

</style>
<div class="home">
    <div class="card">
        <h3>{I18n.t("confirm.header", {email: email})}</h3>
        <a href="/proceed" class="button"
           on:click|preventDefault|stopPropagation={proceed}>{I18n.t("confirm.link")}</a>
    </div>
</div>