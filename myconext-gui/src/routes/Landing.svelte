<script>
    import {user, config, redirectPath} from "../stores/user";
    import {onMount} from "svelte";
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import Button from "../components/Button.svelte";

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
    .landing {
        display: flex;
        justify-content: center;
        flex-direction: column;
        background-color: white;
        height: auto;
        min-height: 500px;
        padding: 25px;
        align-items: center;
        align-content: center;
    }


    h1 {
        margin-bottom: 35px;
    }

    h2 {
        margin-bottom: 60px;
        color: var(--color-primary-green);
        text-align: center;
    }


</style>


<div class="landing">

    <h1>{I18n.ts("header.title")}</h1>
    <h2>{I18n.ts("landing.info")}</h2>
    <Button label={I18n.ts("landing.login")} className="short" href={I18n.ts("landing.login")} onClick={login}/>

</div>
