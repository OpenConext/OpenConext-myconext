import I18n from "../locale/I18n";
import React, {useState} from "react";
import "./UserMenu.scss";
import {stopEvent} from "../utils/Utils";
import {UserInfo} from "@surfnet/sds";
import {useAppStore} from "../stores/AppStore";
import {logout} from "../api";
import {useNavigate} from "react-router-dom";

export const UserMenu = ({user}) => {

    const [dropDownActive, setDropDownActive] = useState(false);
    const navigate = useNavigate();

    const logoutUser = e => {
        stopEvent(e);
        const {config} = useAppStore.getState();
        logout().then(() => {
            useAppStore.setState(() => ({breadcrumbPath: [], user: {}, controlCode: {}}));
            window.location.href = "/"
        });
    }

    const renderMenu = () => {
        return (<>
                <ul>
                    <li>
                        <a href="/logout" onClick={logoutUser}>{I18n.t(`header.links.logout`)}</a>
                    </li>
                </ul>
            </>
        )
    }

    return (
        <div className="user-menu"
             tabIndex={1}
             onBlur={() => setTimeout(() => setDropDownActive(false), 325)}>
            <UserInfo isOpen={dropDownActive}
                      children={renderMenu()}
                      userName={user.displayName}
                      organisationName={user.schacHomeOrganization}
                      toggle={() => setDropDownActive(!dropDownActive)}
            />
        </div>
    );


}
