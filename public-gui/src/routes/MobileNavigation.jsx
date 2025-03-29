import {Navigation} from "../components/Navigation.jsx";
import "./MobileNavigation.scss";
import {useLocation} from "react-router";

export const MobileNavigation = () => {

    const currentLocation = useLocation();

    return (
        <div className="mobile-navigation-container">
            <Navigation mobile={true} path={currentLocation.pathname}/>
        </div>
    );
}