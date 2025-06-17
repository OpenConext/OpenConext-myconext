import I18n from "../locale/I18n.js";
import "./ServiceDesk.scss"
import FrontDesk from "../assets/frontdesk.svg";
import {useEffect} from "react";

export const ServiceDesk = () => {

    useEffect(() => {
        window.scroll(0, 0);
    }, []);

    return (
        <div className="servicedesk-container">
            <div className="servicedesk">
                <div className="top">
                    <div className="top-right">
                        <h1 className="title">
                            {I18n.t("servicedesk.title")}
                        </h1>
                        <p className="info"
                           dangerouslySetInnerHTML={{__html: I18n.t("servicedesk.info")}}/>
                    </div>
                    <img src={FrontDesk} className="frontdesk" alt="frontdesk"/>
                </div>
            </div>
        </div>
    );
}