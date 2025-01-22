import EHERKENNING from "../icons/verify/eIDAS_color.svg?raw";
import SERVICE_DESK from "../icons/frontdesk.svg?raw";
import UNKNOWN from "../icons/verify/banks/image-not-found.svg?raw";
import {isEmpty} from "../utils/utils";


export function logo(issuer) {
    if (isEmpty(issuer)) {
        return UNKNOWN;
    }
    if (issuer.logo) {
        return issuer.logo;
    }
    if (issuer.id && issuer.id.toLowerCase() === "eherkenning") {
        return EHERKENNING;
    }
    if (issuer.id && issuer.id.toLowerCase() === "servicedesk") {
        return SERVICE_DESK;
    }
    return UNKNOWN;
}
