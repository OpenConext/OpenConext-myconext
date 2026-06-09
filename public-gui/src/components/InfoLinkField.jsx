import {useState} from "react";
import "./InfoLinkField.scss"
import CaretUp from "../assets/caret_up.svg?react";
import CaretDown from "../assets/caret_down.svg?react";

export const InfoLinkField = ({id, title, info, children, isOpen, onToggle}) => {

    const [internalOpen, setInternalOpen] = useState(false);

    const controlled = isOpen !== undefined && onToggle !== undefined;
    const open = controlled ? isOpen : internalOpen;

    const handleToggle = () => {
        const nextOpen = !open;
        if (controlled) {
            onToggle(id);
        } else {
            setInternalOpen(nextOpen);
        }
        if (nextOpen && id) {
            history.replaceState(null, "", "#" + id);
        } else if (!nextOpen && id) {
            history.replaceState(null, "", location.pathname + location.search);
        }
    };

    return (
        <div className="info-link-field" id={id}>
            <div className="info-link-field-inner" onClick={handleToggle}>
                <span className={`title ${open ? "open" : "collapsed"}`}>
                    {title}
                </span>
                <button className={`${open ? "open" : "collapsed"}`}>
                    {open ? <CaretUp/> : <CaretDown/>}
                </button>
            </div>
            {(open && info) && <p className="info-link-field-content" dangerouslySetInnerHTML={{__html: info}}/>}
            {(open && !info) && <div className="info-link-field-content">{children}</div>}
        </div>
    );
}
