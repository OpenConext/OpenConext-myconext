import verify from "../assets/verify.svg";
import {Background} from "../components/Background.jsx";
import {InfoLinkField} from "../components/InfoLinkField.jsx";
import {useEffect} from "react";
import {Link} from "react-router";

export const Verify_NL = () => {

    useEffect(() => {
        window.scroll(0, 0);
    }, []);

    return (
        <div className="verify-container">
            <div className="verify">
                <div className="top">
                    <img src={verify} className="verify" alt=""/>
                    <div className="top-right">
                        <h1 className="title small">eduID</h1>
                        <h2>Verifieer je identiteit</h2>
                    </div>
                </div>
            </div>
            <Background>
                <div className="card">
                    <p className="info">
                        Met een geverifieerd eduID kun je bewijzen wie je bent bij onderwijs- en
                        onderzoeksinstellingen. Er zijn vier manieren om je te verifiëren, kies de optie die voor
                        jou het beste werkt.
                    </p>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>Verificatie methoden</h2>
                    <InfoLinkField title="Via je instelling">
                        <p>Ben je student of medewerker? Koppel je eduID aan het account van je instelling.</p>
                        <ol>
                            <li>Ga naar mijn.eduid.nl en log in.</li>
                            <li>Ga naar Je gegevens en klik op Bevestig je identiteit of Voeg een organisatie
                                toe.
                            </li>
                            <li>Klik op Gebruik je school- of werkaccount en volg de stappen.</li>
                            <li>Je eduID is direct geverifieerd.</li>
                        </ol>
                        <p>Je hoeft dit maar één keer te doen. Heb je je identiteit al eerder bevestigd? Dan hoef
                            je deze stappen niet te volgen.</p>
                        <p>Ben je niet aangesloten bij een instelling? Gebruik dan een van de onderstaande manieren
                            om je eduID te verifiëren.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Via je bank (iDIN)">
                        <p>iDIN is een systeem waarmee je jezelf online kunt identificeren via je bank.</p>
                        <ol>
                            <li>Ga naar mijn.eduid.nl en log in.</li>
                            <li>Ga naar Je gegevens en klik op Bevestig je identiteit.</li>
                            <li>Klik op Gebruik je bank.</li>
                            <li>Kies je bank, log in en bevestig. Je deelt alleen je persoonsgegevens, geen
                                financiële gegevens.
                            </li>
                            <li>Je eduID is direct geverifieerd.</li>
                        </ol>
                        <p>Bijna alle Nederlandse banken doen mee aan iDIN. Controleer op idin.nl of jouw bank is
                            aangesloten.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Via je Europees identiteitsbewijs (eIDAS)">
                        <p>eIDAS is een Europese wet die zorgt voor veilige en betrouwbare digitale identificatie
                            binnen Europa. Ben je een Europese burger (niet Nederlands)? Dan kun je met het digitale
                            identiteitsmiddel van je eigen land, vergelijkbaar met DigiD, je eduID-account
                            verifiëren.</p>
                        <ol>
                            <li>Ga naar mijn.eduid.nl en log in.</li>
                            <li>Ga naar Je gegevens en klik op Bevestig je identiteit.</li>
                            <li>Klik op Gebruik een Europees ID.</li>
                            <li>Selecteer je land en volg de stappen.</li>
                            <li>Je eduID is direct geverifieerd.</li>
                        </ol>
                        <p>Niet alle Europese landen zijn aangesloten. Bekijk de actuele lijst op de website van de
                            overheid. Ben je Nederlander? Gebruik dan iDIN of je instellingsaccount.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Bij een eduID servicedesk">
                        <p>Kun je geen gebruik maken van de andere opties? Verifieer je identiteit in persoon bij
                            een <Link to="/servicedesk">servicedesk</Link>. Let op: de servicedesk is momenteel alleen beschikbaar voor deelnemers
                            aan pilots met eduBadges.</p>
                        <ol>
                            <li>Ga naar mijn.eduid.nl en log in.</li>
                            <li>Ga naar Je gegevens en klik op Bevestig je identiteit.</li>
                            <li>Klik dan Neem contact op met de servicedesk en volg de stappen om een
                                verificatiecode aan te maken.
                            </li>
                            <li>Neem de verificatiecode en een geldig identiteitsbewijs mee naar een servicedesk
                                van je instelling.
                            </li>
                            <li>Laat je eduID verifiëren.</li>
                        </ol>
                        <p>Op dit moment zijn er servicedesks beschikbaar bij:</p>
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
                        <p>Staat je instelling er niet bij? Controleer eerst of je een andere verificatiemethode
                            kunt gebruiken. Zo niet, neem dan contact op met je instelling.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>Veelgestelde vragen</h2>
                    <InfoLinkField title="Mijn bank doet niet mee aan iDIN. Wat nu?">
                        <p>Kies een andere verificatiemethode, zoals via je instelling. Welke banken zijn
                            aangesloten vind je op idin.nl.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Ik heb geen geldig identiteitsbewijs. Kan ik nog verifiëren?">
                        <p>Zonder geldig identiteitsbewijs is verificatie via de servicedesk niet mogelijk. Vraag
                            eerst een nieuw document aan, of gebruik een andere methode zoals je instellingsaccount
                            of bank. Een studentenkaart is geen geldig identiteitsbewijs.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Mijn instelling heeft geen eduID servicedesk">
                        <p>Staat de instelling waar je een edubadge van krijgt niet in de lijst van beschikbare
                            servicedesks? Dan kun je deze verificatiemethode helaas niet gebruiken. Neem contact op
                            met je instelling.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Kan ik mijn naam aanpassen na verificatie?">
                        <p>Je roepnaam kun je altijd zelf wijzigen in Mijn eduID. Je geverifieerde naam pas je aan
                            bij de partij waarmee je je hebt geverifieerd, je bank, instelling, of de autoriteit
                            van je land.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Ik krijg een foutmelding. Wat kan ik doen?">
                        <ul>
                            <li>Probeer het later opnieuw, je bank of Europese identiteitsdienst kan tijdelijk niet
                                bereikbaar zijn.
                            </li>
                            <li>Heb je meerdere eduID's aangemaakt? Het is niet mogelijk om meerdere accounts op
                                dezelfde manier te verifiëren.
                            </li>
                            <li>Kom je er niet uit? Stuur een e-mail naar <a
                                href="mailto:help@eduid.nl">help@eduid.nl</a>.
                            </li>
                        </ul>
                    </InfoLinkField>
                    <InfoLinkField title="Ik ben Nederlander. Kan ik verifiëren met een Europees identiteitsbewijs?">
                        <p>Nee, de eIDAS-optie is bedoeld voor burgers uit andere Europese landen. Nederlanders
                            verifiëren via hun bank (iDIN) of via hun onderwijs- of onderzoeksinstelling.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom">
                    <h2>Ik heb een andere vraag</h2>
                    <p className="info">Staat je vraag er niet tussen? Stuur dan een e-mail naar <a
                        href="mailto:help@eduid.nl">help@eduid.nl</a>. We helpen je graag en leggen je uit hoe het werkt.</p>
                </div>
            </Background>
        </div>
    );
}
