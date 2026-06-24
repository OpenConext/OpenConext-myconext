import FrontDesk from "../assets/frontdesk.svg";
import {Background} from "../components/Background.jsx";
import {useEffect} from "react";

export const ServiceDesk_NL = () => {

    useEffect(() => {
        window.scroll(0, 0);
    }, []);

    return (
        <div className="servicedesk-container">
            <div className="servicedesk">
                <div className="top">
                    <img src={FrontDesk} className="frontdesk" alt=""/>
                    <div className="top-right">
                        <h2 className="title">eduID Servicedesk</h2>
                    </div>
                </div>
            </div>
            <Background>
                <div className="card">
                    <p className="info">
                        De servicedesk is een verificatiemethode voor wie geen gebruik kan maken van de andere
                        opties: via je instelling, je bank (iDIN) of een Europees identiteitsbewijs (eIDAS).
                    </p>
                    <div className="callout">
                        <p className="quote"><strong>Let op:</strong> de servicedesk is op dit moment alleen beschikbaar voor
                            deelnemers aan pilots met eduBadges. Ontvang je een badge en heeft je instelling je
                            gevraagd je account te verifiëren? Dan ben je hier op de juiste plek.</p>
                    </div>
                </div>
                <div className="card bottom">
                    <h2>Beschikbare servicedesks</h2>
                    <p className="info">Op dit moment zijn er servicedesks beschikbaar bij:</p>
                    <table className="servicedesk-table">
                        <thead>
                        <tr>
                            <th>Instelling</th>
                            <th>Voor wie</th>
                            <th>Doel</th>
                            <th>Contact</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>TU Delft</td>
                            <td>Alleen voor studenten van TU Delft</td>
                            <td>Toekennen van microcredentials</td>
                            <td><a href="mailto:learningforlife@tudelft.nl">learningforlife@tudelft.nl</a></td>
                        </tr>
                        <tr>
                            <td>Radboud Universiteit</td>
                            <td>Alleen voor studenten van de RU</td>
                            <td>Toekennen van microcredentials</td>
                            <td><a href="mailto:onderwijsvoorprofessionals@ru.nl">onderwijsvoorprofessionals@ru.nl</a>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <p className="info">
                        Staat je instelling er niet bij? Controleer eerst of je een van de andere
                        verificatiemethodes kan gebruiken. Zo niet, neem dan contact op met je instelling.
                    </p>
                </div>
            </Background>
        </div>
    );
}
