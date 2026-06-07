import {useState} from "react";
import "./InfoLinkField.scss"
import CaretUp from "../assets/caret_up.svg?react";
import CaretDown from "../assets/caret_down.svg?react";

export const InfoLinkField = ({title, info, children}) => {

    const [collapse, setCollapse] = useState(false)

    return (
        <div className="info-link-field">
            <div className="info-link-field-inner" onClick={() => setCollapse(!collapse)}>
                <span className={`title ${collapse ? "open" : "collapsed"}`}>
                    {title}
                </span>
                <button className={`${collapse ? "open" : "collapsed"}`}>
                    {collapse ?  <CaretUp/>: <CaretDown/>}
                </button>
            </div>
            {(collapse && info) && <p className="info-link-field-content" dangerouslySetInnerHTML={{__html: info}}/>}
            {(collapse && !info) && <div className="info-link--field-content">{children}</div>}
        </div>
    );
}
