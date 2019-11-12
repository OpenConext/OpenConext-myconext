<script>
    import logo from "./logo.svg";
    import I18n from "i18n-js";
    import {onMount} from 'svelte';

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
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
    }

    .card {
        display: flex;
        flex-direction: column;
        border: 1px solid #dadce0;
        border-radius: 4px;
        background-color: white;
        height: auto;
        min-height: 500px;
        width: auto;
        min-width: 500px;
    }

    .container {
        display: flex;
        flex-direction: column;
        align-items: center;
        height: 100%;
        padding: 25px;
    }

    button {
        border-radius: 4px;
        padding: 10px 20px;
        display: inline-block;
        font-size: larger;
        background-color: #5da7c5;
        color: whitesmoke;
    }

    .logo {
        padding: 40px;
        margin-top: auto;
    }

    :global(.logo) svg {
        height: 51px;
        width: 122px;
    }

    h3 {
        color: #767676;
        margin-bottom: 35px;
    }

</style>
<div class="home">
    <div class="card">
        <div class="container">
            <div class="logo">
                {@html logo}
            </div>
            <h3>{I18n.t("confirm.header", {email: email})}</h3>
            <button on:click={proceed}>{I18n.t("confirm.link")}</button>
        </div>
    </div>
</div>