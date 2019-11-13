<script>
    import {user} from "../stores/user";
    import I18n from "i18n-js";
    import {me} from "../api/index";
    import {navigate} from "svelte-routing";

    const login = () => {
        window.location.href = `/login?redirect_url=${encodeURIComponent(window.location.href)}`;
    };


</script>

<style>
    .wrapper {
        position: relative;
        flex: 1 0 auto;
    }

    .top-bar {
        width: 100%;
        min-height: 250px;
        height: 250px;
        position: absolute;
        top: 0;
        z-index: -5;
        background: #efefef;
    }

    .container {
        max-width: 1140px;
        width: 100%;
        padding-right: 15px;
        padding-left: 15px;
        margin-right: auto;
        margin-left: auto;
    }

    .top-nav {
        height: 100px;
        display: flex;
        align-items: center;
        margin-left: 16px;
        position: relative;
    }

    .row {
        width: 100%;
        flex-wrap: wrap;
        margin-right: 0;
        margin-left: 0;
    }

    .top-nav h2 {
        overflow: hidden;
        white-space: nowrap;
        z-index: 10;
        margin: 0;
    }

    h2.title {
        font-family: "Montserrat", sans-serif;
        padding: 0 15px;
        font-size: 36px;
        line-height: 50px;
        font-weight: 400;
        color: #772583;
    }

    h3 {
        font-family: "Montserrat", sans-serif;
        font-weight: 400;
        font-size: 28px;
        color: #676767;
    }

    .block {
        padding: 30px 45px;
        min-height: 200px;
        border-radius: 40px;
        background: #fff;
    }

    p {
        line-height: 28px;
        margin-top: 0;
        margin-bottom: 1rem;
    }

    .button {
        height: 44px;
        margin: 30px 0;
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


<div class="wrapper">
    <div class="top-bar"></div>
    <div class="container body-content">

        <div class="top-nav row">
            <h2 class="title">Log in op SURFopzichter</h2>
        </div>

        <div class="block">

            <p>SURFopzichter is het selfservice portal voor ICT beheerders en contactpersonen van de op SURFnet
                aangesloten instellingen.<br>
                Voor toegang tot SURFdashboard dient u minimaal een SURFnet Autorisatie Rol te hebben.</p>


            <h3>Bent u een geautoriseerd medewerker?</h3>
            <p>Log in bij SURFnet met de login gegevens van uw instelling om door te gaan naar het
                SURFdashboard</p>
            <input type="submit" id="submit" class="button"
                   value="Inloggen">
            <p/>
            <h3>Geen autorisatie</h3>
            <p>
                Bent u wel medewerker maar heeft u geen autorisatie. Vraag deze dan aan via uw
                instellingscontactpersoon.<br>
                Weet u niet wie dit is? Neem dan contact op met <a
                    href="mailto:klantsupport@surfnet.nl">klantsupport</a>
                van SURFnet.</p>
        </div>
    </div>
</div>
