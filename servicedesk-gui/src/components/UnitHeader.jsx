import React from "react";
import "./UnitHeader.scss";
import Frontdesk from "../icons/frontdesk.svg";
export const UnitHeader = ({obj, children}) => {

    return (
        <div className="unit-header-container">
            <div className="unit-header">
                <div className={`image ${obj.style || ""}`}>
                    <Frontdesk/>
                </div>
                <div className="obj-name">
                    <h1>{obj.name}</h1>
                    {obj.description}
                    {children}
                </div>
            </div>
        </div>
    )
}
