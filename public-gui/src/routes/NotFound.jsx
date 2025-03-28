import "./NotFound.scss";
import NotFoundLogo from "../assets/undraw_page_not_found_re_e9o6.svg";
import I18n from "../locale/I18n";

const NotFound = () => (
    <div className={"not-found"}>
        <img src={NotFoundLogo} alt={I18n.t("notFound.alt")}/>
    </div>
);
export default NotFound;