import {Link, useNavigate} from "react-router";
import eduIDLogo from '../assets/logo_eduID.svg'
import {useState} from "react";
import {stopEvent} from "../utils/Utils.js";
import I18n from "../locale/I18n.js";


const tabNames = ["home", "about", "support"]

export const Header = () => {

    const navigate = useNavigate();
    const [tab, setTab] = useState("home")


    const doNavigate = (e, tabName) => {
        stopEvent(e);
        setTab(tabName);
        navigate(`/${tabName}`)
    }

    return (
        <div className="header-container">
            <div className="header-inner">
                <Link className="logo" to={"/"}>
                    <img src={eduIDLogo} className="logo" alt="eduID logo" />
                </Link>
                {tabNames.map(tabName => <a key={tabName}
                                            href={`/${tabName}`}
                                            className={tabName === tab ? "active" : ""}
                                            onClick={e => doNavigate(e, tabName)} >
                    {I18n.t(`tabs.${tabName}`)}
                </a>)}

            </div>
        </div>
    );
}

