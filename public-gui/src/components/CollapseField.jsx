import React, {useState} from "react";
import "./CollapseField.scss"
import caretUp from "../assets/caret_up.svg";
import caretDown from "../assets/caret_down.svg";

export const CollapseField = ({title, info}) => {

    const [collapse, setCollapse] = useState(false)

    return (
        <div className="collapse-field">
            <div className="collapse-field-inner"
                 onClick={() => setCollapse(!collapse)}>
            <span className={`${collapse ? "collapsed" : "open"}`}>
                {title}
            </span>
                <img src={collapse ?  caretUp: caretDown}
                     className="caret" alt="caret"/>
            </div>
            {collapse && <p className="collapsed"
                            dangerouslySetInnerHTML={{__html: info}}/>}
        </div>
    );
}
