import "./Forbidden.scss";
import React from "react";
import WarningLogo from "../icons/undraw_warning_qn4r.svg";
import I18n from "../locale/I18n";
import {Button} from "@surfnet/sds";
import {logout} from "../api/index.js";
import {useAppStore} from "../stores/AppStore.js";

const Forbidden = () => {

    const logoutUser = () => {
        const {config} = useAppStore.getState();
        logout().then(() => {
            useAppStore.setState(() => ({breadcrumbPath: [], user: {}, controlCode: {}}));
            window.location.href = `${config.idpBaseUrl}/doLogout?param=${encodeURIComponent("logout=true")}`;
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