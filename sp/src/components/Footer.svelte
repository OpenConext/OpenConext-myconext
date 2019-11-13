<script>
    import mail from "./mail-white.svg";
    import phone from "./phone-white.svg";
    import marker from "./pin-marker.svg";
    import I18n from "i18n-js";
</script>

<style>
    .footer {
        margin-top: auto;
    }

    .top {
        position: relative;
        display: flex;
        align-items: center;
    }

    .block-holder {
        width: 100%;
        height: 150px;
        position: relative;
    }

    .block-grey-sm {
        height: 50px;
        width: 50px;
        background: #EDEDED;
        position: absolute;
        right: 300px;
        bottom: 0;
        z-index: 2;
    }

    .block-white {
        height: 50px;
        width: 50px;
        border-radius: 0 0 60px 0;
        background: #FFF;
    }

    .block-grey-xl {
        height: 125px;
        width: 300px;
        border-radius: 60px 0 0 0;
        background: #EDEDED;
        position: absolute;
        right: 0;
        bottom: 0;
        z-index: 1;
    }

    .center {
        height: 150px;
        width: 100%;
        background: #EDEDED;
        float: right;
    }

    .bottom {
        width: 100%;
        padding: 20px 0;
        background: #0062B0;
        float: right;
    }

    .container {
        padding: 0 45px;
        max-width: 1140px;
        width: 100%;
        margin-right: auto;
        margin-left: auto;
        display: flex;
        align-items: center;
    }
    @media only screen and (max-width: 768px) {
        .container {
            flex-direction: column;
            align-items: normal;
        }
    }
    .footer a {
        font-weight: 300;
        font-size: 16px;
        color: #fff;
        margin: 0 30px 0 5px;
        text-decoration: none;
    }
</style>

<div class="footer">
    <div class="top">
        <div class="block-holder">
            <div class="block-grey-sm">
                <div class="block-white"></div>
            </div>
            <div class="block-grey-xl"></div>
        </div>
    </div>
    <div class="center"></div>
    <div class="bottom">
        <div class="container">
            <span>
            {@html marker}
                <a href="https://www.google.com/maps/place/SURFnet/@52.0890566,5.1112714,17z/data=!3m1!4b1!4m5!3m4!1s0x47c66f5d2eed70a7:0x9a30e9ae49e2b7f3!8m2!3d52.0890566!4d5.1134654"
                   title="Bekijk de locatie van SURFnet via GoogleMaps" target="_blank"
                   class="icon-pin-marker">SURFnet bv | Postbus 19035, 3501 DA Utrecht</a>
                </span>
            <span>
            {@html phone}
                <a href="tel:+31887873000" title="Neem telefonisch contact op via +31 88 787 30 00"
                   class="icon-phone-white">+31 88 787 30 00</a>
            </span>
            <span>
            {@html mail}
                <a href="mailto:surfconext@surfnet.nl" title="Verstuur een e-mail naar surfconext@SURFnet.nl"
                   class="icon-mail-white">surfconext@SURFnet.nl</a>
                </span>

        </div>
    </div>
</div>