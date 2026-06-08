import FrontDesk from "../assets/frontdesk.svg";
import {Background} from "../components/Background.jsx";
import {useEffect} from "react";

export const ServiceDesk_EN = () => {

    useEffect(() => {
        window.scroll(0, 0);
    }, []);

    return (
        <div className="servicedesk-container">
            <div className="servicedesk">
                <div className="top">
                    <div className="top-right">
                        <h1 className="title">eduID Service Desk</h1>
                    </div>
                    <img src={FrontDesk} className="frontdesk" alt=""/>
                </div>
            </div>
            <Background>
                <div className="card">
                    <p className="info">
                        The service desk is a verification method for those who cannot use the other options:
                        through your institution, your bank (iDIN), or a European identity document (eIDAS).
                    </p>
                    <div className="callout">
                        <p className="quote"><strong>Please note:</strong> the service desk is currently only available to
                            participants in eduBadges pilots. Are you receiving a badge and has your institution
                            asked you to verify your account? Then you are in the right place.</p>
                    </div>
                </div>
                <div className="card bottom">
                    <h2>Available service desks</h2>
                    <p className="info">At this moment, service desks are available at:</p>
                    <table className="servicedesk-table">
                        <thead>
                        <tr>
                            <th>Institution</th>
                            <th>For whom</th>
                            <th>Purpose</th>
                            <th>Contact</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>TU Delft</td>
                            <td>Only for TU Delft students</td>
                            <td>Awarding microcredentials</td>
                            <td><a href="mailto:learningforlife@tudelft.nl">learningforlife@tudelft.nl</a></td>
                        </tr>
                        <tr>
                            <td>Radboud Universiteit</td>
                            <td>Only for RU students</td>
                            <td>Awarding microcredentials</td>
                            <td><a href="mailto:onderwijsvoorprofessionals@ru.nl">onderwijsvoorprofessionals@ru.nl</a>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <p className="info">
                        Is your institution not listed? First check whether you can use one of the other
                        verification methods. If not, please contact your institution.
                    </p>
                </div>
            </Background>
        </div>
    );
}
