import about from "../assets/about.svg";
import {Background} from "../components/Background.jsx";
import {InfoLinkField} from "../components/InfoLinkField.jsx";
import {Link, useNavigate} from "react-router";
import {useFragmentOpen} from "../hooks/useFragmentOpen.js";
import {Button} from "@surfnet/sds";
import {useAppStore} from "../stores/AppStore.js";
import I18n from "../locale/I18n.js";
import {QRCode} from "react-qr-code";
import appStore from "../assets/app_store.svg";
import googlePlay from "../assets/google_play.svg";

export const About_NL = () => {

    const config = useAppStore((state) => state.config);
    const navigate = useNavigate();
    const {openId, handleToggle} = useFragmentOpen();
    const isMobile = "ontouchstart" in window || navigator.maxTouchPoints > 0;

    return (
        <div className="about-container">
            <div className="about">
                <div className="top">
                    <img src={about} className="about" alt=""/>
                    <div className="top-right">
                        <h2>Eén account voor onderwijs- en onderzoeksinstellingen</h2>
                    </div>
                </div>
            </div>
            <Background>
                <div className="card">
                    <h2>Wat is eduID?</h2>
                    <p className="info">
                        eduID is jouw persoonlijke account waarmee je inlogt bij onderwijs- en onderzoeksinstellingen
                        in Nederland, of je nu student, medewerker of professional bent. Je eduID blijft van jou, ook
                        als je van instelling of baan verandert.
                    </p>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>Hoe gebruik je eduID?</h2>
                    <InfoLinkField id="1-create-an-eduid" title="1. Maak een eduID aan"
                                   isOpen={openId === "1-create-an-eduid"} onToggle={handleToggle}>
                        <p>Klik op 'eduID aanmaken' en volg de stappen.</p>
                        <p>Gebruik een <strong>persoonlijk e-mailadres</strong> dat je blijft gebruiken, ook als je van
                            school of baan
                            verandert.</p>
                        <Button onClick={() => window.location.href = `${config.accountBaseUrl}/register`}
                                txt={I18n.t("header.register")}/>
                    </InfoLinkField>
                    <InfoLinkField id="2-verify-your-identity-with-an-official-party"
                                   title="2. Verifieer je identiteit bij een officiele partij"
                                   isOpen={openId === "2-verify-your-identity-with-an-official-party"}
                                   onToggle={handleToggle}>
                        <p>Om je eduID te kunnen gebruiken, moet je in de meeste gevallen eerst bevestigen wie je
                            bent. Dat doe je via een van de volgende methodes:</p>
                        <p className="section-label top"><strong>Via je Nederlandse onderwijs- of
                            onderzoeksinstelling</strong></p>
                        <p>Door je instelling te koppelen, voeg je in één keer het volgende toe aan je eduID:</p>
                        <ul className="verify-options">
                            <li>Je volledige naam, bevestigd door een officiële partij</li>
                            <li>Bewijs dat je student of medewerker bent</li>
                            <li>Je huidige instelling</li>
                        </ul>
                        <p className="section-label"><strong>Zo doe je het</strong></p>
                        <p>Open de eduID-app of ga naar Mijn eduID en kies 'Koppel je instelling'. Of klik op
                            'bevestig je identiteit' en vervolgens op 'gebruik je school- of werkaccount'.</p>
                        <p className="separator">Je logt in bij je instelling, waarna die je naam en rol automatisch met
                            ons deelt.</p>
                        <p className="quote">Let op: je instellingsgegevens worden na 6 maanden automatisch verwijderd.
                            Daarna kun je
                            je instelling eenvoudig opnieuw koppelen.</p>
                        <p className="section-label top"><strong>Niet aangesloten bij een instelling?</strong> Dan kun
                            je op de volgende manieren je eduID
                            verifiëren:</p>
                        <ul>
                            <li><strong>Via je bank (iDIN)</strong> - voor klanten van een Nederlandse bank</li>
                            <li><strong>Europees identiteitsbewijs (eIDAS)</strong> - voor burgers uit andere Europese
                                landen
                            </li>
                            <li><strong><Link to={"/servicedesk"}>Servicedesk</Link></strong> - alleen voor deelnemers
                                aan pilots met eduBadges
                            </li>
                        </ul>
                        <Button onClick={() => navigate("/verify")} txt={"Bekijk hoe je je eduID verifieert"}/>
                    </InfoLinkField>
                    <InfoLinkField id="3-log-in-to-websites-and-other-online-services"
                                   title="3. Log in bij websites en andere online diensten"
                                   isOpen={openId === "3-log-in-to-websites-and-other-online-services"}
                                   onToggle={handleToggle}>
                        <p>Met je eduID kun je inloggen bij websites en andere online diensten in Nederland. We raden
                            je aan om hiervoor de eduID-app te gebruiken, omdat dit de meest veilige en eenvoudige
                            manier is. Met de app log je namelijk zonder wachtwoord in. En door de
                            tweefactorauthenticatie (2FA) is het extra veilig.</p>
                        <div className="store-container">
                            <div className="store-container-content">
                                {!isMobile &&
                                    <QRCode
                                        size={140}
                                        style={{height: "auto"}}
                                        value={`${window.location.origin}/install-app`}
                                    />}
                                <div className="inner-store-container">
                                    <a href={I18n.t("home.apple")}>
                                        <img src={appStore} className="appStore" alt={I18n.t("home.appStoreAlt")}/>
                                    </a>
                                    <a href={I18n.t("home.google")}>
                                        <img src={googlePlay} className="googlePlay"
                                             alt={I18n.t("home.googlePlayAlt")}/>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </InfoLinkField>
                </div>
                <div className="card bottom">
                    <h2>Waar vind je je account en gegevens terug?</h2>
                    <p className="info">
                        In <a href="https://mijn.eduid.nl">Mijn eduID</a> vind je al je gegevens, zoals je naam,
                        e-mailadres en beveiligingsinstellingen.
                        Je kunt daar ook zien bij welke diensten je bent ingelogd met eduID, en gegevens aanpassen
                        wanneer je dat wilt.
                    </p>
                </div>
            </Background>
        </div>
    );
}
