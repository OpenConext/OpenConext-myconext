import {stopEvent} from "../utils/Utils.js";

export const AnchorLink = ({label, identifier}) => {

    const smoothScroll = e => {
        stopEvent(e);
        const anchor = document.getElementById(identifier);
        const offsetTop = anchor.getBoundingClientRect().top + window.pageYOffset;
        window.scroll({
            top: offsetTop,
            behavior: "smooth"
        })
    };
    return (
        <a className="anchor-field"
           href={`#${identifier}`}
           onClick={e => smoothScroll(e)}>
            {label}
        </a>
    );
}
