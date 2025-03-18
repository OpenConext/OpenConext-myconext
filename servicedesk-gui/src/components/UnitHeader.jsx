import React, {useState} from "react";
import "./UnitHeader.scss";
import Frontdesk from "../icons/frontdesk.svg";

export const UnitHeader = ({children}) => {

    return (
        <div className="unit-header-container">
            <div className="unit-header">
                <div className="obj-name">
                    {children}
                </div>
            </div>
        </div>
    )
}
