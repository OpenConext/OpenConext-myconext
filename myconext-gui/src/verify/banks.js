import ABNANL2A from "../icons/verify/banks/abn_amro.svg";
import ASNBNL21 from "../icons/verify/banks/asn.svg";
import BUNQNL2A from "../icons/verify/banks/bunq.svg";
import INGBNL2A from "../icons/verify/banks/ing.svg";
import RABONL2U from "../icons/verify/banks/rabo.svg";
import RBRBNL21 from "../icons/verify/banks/regiobank.svg";
import SNSBNL2A from "../icons/verify/banks/sns.svg";


export function logo(id) {
    switch(id) {
        case "ABNANL2A": return ABNANL2A;
        case "ASNBNL21": return ASNBNL21;
        case "BUNQNL2A": return BUNQNL2A;
        case "INGBNL2A": return INGBNL2A;
        case "RABONL2U": return RABONL2U;
        case "RBRBNL21": return RBRBNL21;
        case "SNSBNL2A": return SNSBNL2A;
    }
}
