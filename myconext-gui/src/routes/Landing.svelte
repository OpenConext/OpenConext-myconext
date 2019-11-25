<script>
    import {user, config, redirectPath} from "../stores/user";
    import {onMount} from "svelte";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";

    onMount(() => {
                if ($user.id) {
                    navigate("/");
                }
            });

    const login = () => {
        window.location.href = `${$config.loginUrl}?redirect_path=${encodeURIComponent($redirectPath || "/")}`;
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
        width: 1080px;
        padding: 25px;
    }

    .button {
        border: 1px solid #818181;
        width: 70px;
        background-color: #c7c7c7;
        border-radius: 2px;
        padding: 10px 20px;
        display: inline-block;
        color: black;
        text-decoration: none;
        cursor: pointer;
        text-align: center;
    }

    h2 {
        color: #767676;
        margin-bottom: 35px;
    }

    p {
        color: #767676;
        margin-bottom: 35px;
    }

    .button {
        height: 44px;
        margin: 30px 0 55px 0;
        background-color: #0077C8;
        min-width: 400px;
        display: inline-block;
        text-align: center;
        vertical-align: middle;
        border: 1px solid #007bff;
        padding: .375rem .75rem;
        font-size: 18px;
        border-radius: .25rem;
        cursor: pointer;
        transition: color .15s ease-in-out, background-color .15s ease-in-out, border-color .15s ease-in-out, box-shadow .15s ease-in-out;
        color: #fff;
    }

    .button:hover {
        background: #004C79;
        border-color: #0062cc;
    }

</style>


<div class="home">
    <div class="card">

        <h2 class="title">{I18n.ts("landing.loginHeader")}</h2>

        <div class="block">

            <p>{I18n.ts("landing.info")}</p>
            <span class="button" on:click={login}>{I18n.ts("landing.login")}</span>
            <h2>{I18n.ts("landing.questionsTitle")}</h2>
            <p>{I18n.ts("landing.whatCanYouDo")}</p>
            <p>{@html I18n.ts("landing.questions")}</p>


        </div>
    </div>
</div>
