import {Navigation} from "../components/Navigation.jsx";
import "./MobileNavigation.scss";

export const MobileNavigation = () => {

    return (
        <div className="mobile-navigation-container">
            <Navigation mobile={true}/>
        </div>
    );
}