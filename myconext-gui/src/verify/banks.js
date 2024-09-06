import EHERKENNING from "../icons/verify/eIDAS_color.svg";
import UNKNOWN from "../icons/verify/banks/image-not-found.svg";
import {isEmpty} from "../utils/utils";


export function logo(issuer) {
    if (isEmpty(issuer)) {
        return UNKNOWN;
    }
    if (issuer.logo) {
        return issuer.logo;
    }
    if (issuer.id === "eherkenning") {
        return EHERKENNING;
    }
    return UNKNOWN;
}
