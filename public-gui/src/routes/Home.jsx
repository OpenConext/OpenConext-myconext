import "./Home.scss";
import phone from "../assets/phone.svg";
import cheering from "../assets/cheering.svg";
import app from "../assets/app.svg";
import qrCode from "../assets/qr_code.svg";
import appStore from "../assets/app_store.svg";
import googlePlay from "../assets/google_play.svg";
import I18n from "../locale/I18n.js";
import {useAppStore} from "../stores/AppStore.js";
import {Button} from "@surfnet/sds";
import {Link} from "react-router";

export const Home = () => {

    const config = useAppStore((state) => state.config);

    return (
        <div className="home-container">
            <div className="home">
                <div className="top">
                    <img src={phone} className="phone" alt="phone"/>
                    <div className="top-right">
                        <h1 className="title">
                            {I18n.t("home.eduID")}
                        </h1>
                        <h3>{I18n.t("home.title")}</h3>
                        <Button onClick={() => window.location.href = `${config.idpBaseUrl}/register`}
                                txt={I18n.t("header.register")}/>
                    </div>
                </div>
            </div>
            <div className="background">
                <div className="card">
                    <img src={cheering} className="cheering" alt="cheering"/>
                    <div className="top-right middle">
                        <h2 className="title">
                            {I18n.t("home.whatCanYouDo")}
                        </h2>
                        <ul>
                            <li>{I18n.t("home.info1")}</li>
                            <li>{I18n.t("home.info2")}</li>
                            <li><Link to={"/about"}>{I18n.t("home.info3")}</Link></li>
                        </ul>
                    </div>
                </div>
                <div className="card bottom">
                    <img src={app} className="app" alt="app"/>
                    <div className="top-right middle">
                        <h2 className="title">
                            {I18n.t("home.eduIdApp")}
                        </h2>
                        <p>{I18n.t("home.appInfo")}</p>
                        <div className="store-container">
                            <img src={qrCode} className="qrCode" alt="qrCode"/>
                            <div className="inner-store-container">
                                <img src={appStore} className="appStore" alt="appStore"/>
                                <img src={googlePlay} className="googlePlay" alt="googlePlay"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}