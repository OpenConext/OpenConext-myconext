import "./Home.scss";
import phone from "../assets/phone.svg";
import cheering from "../assets/cheering.svg";
import app from "../assets/app.svg";
import QRCode from "react-qr-code";
import appStore from "../assets/app_store.svg";
import googlePlay from "../assets/google_play.svg";
import I18n from "../locale/I18n.js";
import {useAppStore} from "../stores/AppStore.js";
import {Button} from "@surfnet/sds";
import {Link} from "react-router";
import {Background} from "../components/Background.jsx";
import {useEffect} from "react";

export const Home = () => {

    const config = useAppStore((state) => state.config);

    const isMobile = "ontouchstart" in window || navigator.maxTouchPoints > 0;

    useEffect(() => {
        window.scroll(0, 0);
    }, []);

    return (
        <div className="home-container">
            <div className="home">
                <div className="top">
                    <img src={phone} className="phone" alt="phone"/>
                    <div className="top-right">
                        <h1 className="title">
                            {I18n.t("home.eduID")}
                        </h1>
                        <h2>{I18n.t("home.title")}</h2>
                        <Button onClick={() => window.location.href = `${config.idpBaseUrl}/register`}
                                txt={I18n.t("header.register")}/>
                    </div>
                </div>
            </div>
            <Background>
                <div className="card row">
                    <img src={cheering} className="cheering" alt="cheering"/>
                    <div className="top-right middle">
                        <h2 className="title">
                            {I18n.t("home.whatCanYouDo")}
                        </h2>
                        <ul>
                            <li>{I18n.t("home.info1")}</li>
                            <li>{I18n.t("home.info2")}</li>
                            <li>{I18n.t("home.info3")}</li>
                        </ul>
                        <p><Link to={"/about"}>{I18n.t("home.readMore")}</Link></p>
                    </div>
                </div>
                <div className="card bottom row">
                    <img src={app} className="app" alt="app"/>
                    <div className="top-right middle">
                        <h2 className="title">
                            {I18n.t("home.eduIdApp")}
                        </h2>
                        <p dangerouslySetInnerHTML={{__html: I18n.t("home.appInfo")}}/>
                        <div className="store-container">
                            {!isMobile &&
                                <QRCode
                                    size={140}
                                    style={{height: "auto"}}
                                    value={`${window.location.origin}/install-app`}
                                />}
                            <div className="inner-store-container">
                                <a href={I18n.t("home.apple")}>
                                    <img src={appStore} className="appStore" alt="appStore"/>
                                </a>
                                <a href={I18n.t("home.google")}>
                                    <img src={googlePlay} className="googlePlay" alt="googlePlay"/>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </Background>
        </div>
    );
}