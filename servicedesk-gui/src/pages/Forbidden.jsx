import "./Forbidden.scss";
import React from "react";
import WarningLogo from "../icons/undraw_warning_qn4r.svg";
import I18n from "../locale/I18n";
import {Button} from "@surfnet/sds";
import {logout} from "../api/index.js";
import {useNavigate} from "react-router-dom";
import {useAppStore} from "../stores/AppStore.js";

const Forbidden = () => {
    const navigate = useNavigate();

    const logoutUser = () => {
        logout().then(() => {
            useAppStore.setState(() => ({breadcrumbPath: [], user: {}, controlCode: {}}));
            navigate("/login");
            setTimeout(() =>
                useAppStore.setState(() => ({user: null, breadcrumbPath: []})), 125);
        });
    }

    return (
        <div className="forbidden-container">
            <div className="forbidden">
                <p>{I18n.t("forbidden.info")}</p>
                <WarningLogo/>
                <div className="actions">
                    <Button txt={I18n.t("forbidden.logout")}
                            onClick={() => logoutUser()}/>
                </div>
            </div>
        </div>)
};
export default Forbidden;