import {useState} from "react";
import "./CollapseField.scss"
import caretUp from "../assets/caret_up.svg";
import caretDown from "../assets/caret_down.svg";

export const CollapseField = ({title, info, children}) => {

    const [collapse, setCollapse] = useState(false)

    return (
        <div className="collapse-field" onClick={() => setCollapse(!collapse)}>
            <div className="collapse-field-inner">
                <span className={`${collapse ? "collapsed" : "open"}`}>
                    {title}
                </span>
                <button>
                    <img src={collapse ?  caretUp: caretDown} className="caret" alt="caret"/>
                </button>
            </div>
            {(collapse && info) && <p className="collapse-field-content" dangerouslySetInnerHTML={{__html: info}}/>}
            {(collapse && !info) && <div className="collapse-field-content">{children}</div>}
        </div>
    );
}
