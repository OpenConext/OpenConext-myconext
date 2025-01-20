import React, {useState} from "react";
import "./UnitHeader.scss";
import logo from "../icons/frontdesk.svg";
import {isEmpty, stopEvent} from "../utils/Utils";

import {Button, ButtonType, MenuButton} from "@surfnet/sds";
import {Link, useNavigate} from "react-router-dom";
import I18n from "../locale/I18n";

export const UnitHeader = ({
                               obj,
                               history,
                               auditLogPath,
                               name,
                               breadcrumbName,
                               firstTime,
                               actions,
                               children,
                               customAction,
                               displayDescription
                           }) => {

    const [showDropDown, setShowDropDown] = useState(false);

    const navigate = useNavigate();
    const performAction = action => e => {
        stopEvent(e);
        !action.disabled && action.perform();
    }

    const otherOptions = (chevronActions, firstTime, auditLogPath, history, queryParam) => {
        return (
            <ul className={"other-options"}>
                {chevronActions.map((action, index) => <li key={index} onClick={performAction(action)}>
                    <a href={`/${action.name}`}>{action.name}</a>
                </li>)}
                {(history && auditLogPath) &&
                    <li onClick={() => navigate(`/audit-logs/${auditLogPath}?${queryParam}`)}>
                        <Link to={`/audit-logs/${auditLogPath}?${queryParam}`}>
                            {I18n.t("home.history")}
                        </Link>
                    </li>}
                {firstTime &&
                    <li onClick={performAction({perform: firstTime})}>
                        <a href={"/" + I18n.t("home.firstTime")}>
                            {I18n.t("home.firstTime")}
                        </a>
                    </li>}
            </ul>
        )
    }

    const queryParam = `name=${encodeURIComponent(breadcrumbName || name)}&back=${encodeURIComponent(window.location.pathname)}`;
    const nonChevronActions = (actions || []).filter(action => action.buttonType !== ButtonType.Chevron);
    const chevronActions = (actions || []).filter(action => action.buttonType === ButtonType.Chevron);
    const showChevronAction = (history && auditLogPath) || firstTime || chevronActions.length > 0;
    return (
        <div className="unit-header-container">
            <div className="unit-header">
                <div className={`image ${obj.style || ""}`}>
                    <img src={logo} className="logo" alt="Vite logo" />
                </div>
                <div className="obj-name">
                    <h1>{obj.name}</h1>
                    {obj.description}
                    {children}
                </div>
                {!isEmpty(actions) &&
                    <div className="action-menu-container">
                        {nonChevronActions.map((action, index) =>
                            <Button key={index}
                                    onClick={() => !action.disabled && action.perform()}
                                    txt={action.name}
                                    cancelButton={action.buttonType === ButtonType.Secondary}/>)
                        }
                        {showChevronAction &&
                            <div tabIndex={1}
                                 onBlur={() => setTimeout(() => setShowDropDown(false), 125)}>
                                <MenuButton txt={I18n.t("home.otherOptions")}
                                            isOpen={showDropDown}
                                            toggle={() => setShowDropDown(!showDropDown)}
                                            buttonType={ButtonType.Secondary}
                                            children={otherOptions(chevronActions, firstTime, auditLogPath, history, queryParam)}/>
                            </div>}
                    </div>}
                {customAction && customAction}
            </div>
        </div>
    )
}
