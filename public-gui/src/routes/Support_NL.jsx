import parachute from "../assets/parachute.svg";
import {Background} from "../components/Background.jsx";
import {InfoLinkField} from "../components/InfoLinkField.jsx";
import {useEffect} from "react";

export const Support_NL = () => {

    useEffect(() => {
        window.scroll(0, 0);
    }, []);

    return (
        <div className="support-container">
            <div className="support">
                <div className="top">
                    <img src={parachute} className="parachute" alt=""/>
                    <div className="top-right">
                        <h2>Voor als je er niet uit komt</h2>
                    </div>
                </div>
            </div>
            <Background>
                <div className="card">
                    <h2>Veelgestelde vragen</h2>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h3>Accountbeheer</h3>
                    <InfoLinkField title="Hoe beheer ik mijn eduID?">
                        <p>Wil je je gegevens bekijken of aanpassen? Ga naar mijn.eduid.nl. Daar beheer je alles wat
                            met je eduID te maken heeft.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Hoe wijzig ik mijn e-mailadres?">
                        <p>Je past je e-mailadres aan via <a href="https://mijn.eduid.nl/">Mijn eduID</a>:</p>
                        <ol>
                            <li>Log in op <a href="https://eduid.nl/">eduid.nl</a>.</li>
                            <li>Ga naar 'Persoonlijke info'.</li>
                            <li>Klik op het 'wijzig'-icoontje naast je e-mailadres.</li>
                        </ol>
                    </InfoLinkField>
                    <InfoLinkField title="Hoe kan ik mijn eduID verwijderen?">
                        <p>Wil je je eduID verwijderen? Log in op mijn.eduid.nl en ga naar 'Account'. Daar kun je je
                            eduID verwijderen.</p>
                        <p>Let op: denk goed na voordat je je eduID verwijdert. Je kunt toegang verliezen tot
                            applicaties waar je nu mee inlogt, en die toegang is niet meer terug te krijgen.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>Persoonlijke gegevens</h2>
                    <InfoLinkField title="Heb ik meerdere eduID's nodig?">
                        <p>Nee. Je hebt maar één eduID nodig.</p>
                        <ul>
                            <li>Je maakt deze aan met je privé e-mailadres.</li>
                            <li>Je voegt daarna organisaties toe, zoals een MBO, HBO of universiteit.</li>
                            <li>Wissel je van studie of werk? Dan blijft je eduID bestaan.</li>
                        </ul>
                        <p>Heb je per ongeluk een eduID aangemaakt met het e-mailadres van je school of organisatie?
                            Dan kun je dit aanpassen via Mijn eduID bij 'Persoonlijke info'.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Mijn naam klopt niet. Wat kan ik doen?">
                        <p>Je kunt je geverifieerde naam niet zelf aanpassen. Je roepnaam kun je wel altijd wijzigen.
                            Wil je je voor- of achternaam wijzigen, dan moet je dit bij de partij doen waarmee je je
                            eduID hebt geverifieerd.</p>
                        <ul>
                            <li>Instelling: neem contact op met je school of universiteit.</li>
                            <li>Nederlandse bank: pas je naam aan bij de bank.</li>
                            <li>Europese ID: ga naar de website van de ID-partij die je hebt gebruikt. Na de
                                wijziging kun je jouw naam opnieuw updaten in eduID. Dit doe je door de gegevens
                                eerst te verwijderen (zie hieronder) en vervolgens de verificatie opnieuw te doen.
                            </li>
                        </ul>
                        <p>Is je naam veranderd omdat je bijvoorbeeld trouwt of van gender verandert? Dan regel je
                            dit meestal eerst via de gemeente. Daarna kun je de naam ook in eduID aanpassen.</p>
                        <p>Zie je alleen een voorletter? Dat kan kloppen. Als je je naam hebt geverifieerd via je
                            bank, dan ontvangen we alleen de voorletter(s) van je naam. Je kunt dan bij 'roepnaam'
                            je volledige naam invullen.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Kan ik mijn geverifieerde gegevens ook verwijderen?">
                        <p>Ja, dat kan maar denk er goed over na. Als je geverifieerde gegevens verwijdert, kun je
                            mogelijk niet meer inloggen bij diensten waar je dat nu wel kunt met je eduID. Zo
                            verwijder je geverifieerde gegevens:</p>
                        <ol>
                            <li>Ga naar Persoonlijke info in je eduID.</li>
                            <li>Klik op het pijltje (v) naast de gegevens die je wilt verwijderen.</li>
                            <li>Klik op Beheer je geverifieerde informatie.</li>
                            <li>Zoek de gegevens op en klik op Verwijder deze informatie.</li>
                        </ol>
                    </InfoLinkField>
                    <InfoLinkField title="Waarom moet ik mijn instelling opnieuw koppelen?">
                        <p>Je instellingsgegevens worden na 6 maanden automatisch verwijderd om je gegevens actueel
                            te houden, bijvoorbeeld als je van instelling of rol wisselt. Je merkt dit meestal
                            doordat je wordt gevraagd opnieuw te koppelen wanneer je ergens probeert in te
                            loggen.</p>
                        <p>Koppel je instelling opnieuw via de eduID-app of Mijn eduID.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Kan ik meerdere organisaties aan mijn eduID koppelen?">
                        <p>Ja, je kunt meerdere onderwijs- of onderzoeksinstellingen aan je eduID koppelen. Dit kan
                            nodig zijn als je bij meerdere instellingen studeert of werkt. Per instelling kun je
                            bewijzen wie je bent en met welke rol (zoals student of medewerker). Ga naar
                            mijn.eduid.nl en kies 'Voeg instelling toe'.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>Inloggen</h2>
                    <InfoLinkField title="Mijn sessie is verlopen. Wat kan ik doen?">
                        <p>Deze foutmelding krijg je als er een probleem is tijdens het inloggen, bijvoorbeeld als
                            je te lang wacht na het klikken op de inlogknop. Log opnieuw in.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Kan ik een andere inlogmethode kiezen?">
                        <p>Ja dat kan. Standaard ontvang je een inlogcode per e-mail. Wil je een andere methode
                            gebruiken? Ga in Mijn eduID naar 'Beveiliging'. Daar zie je je huidige inlogmethode en
                            kun je die aanpassen of een andere toevoegen, zoals een wachtwoord of een passkey.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Wat is een passkey en hoe gebruik ik het?">
                        <p>Een passkey (beveiligingssleutel) is een manier om in te loggen zonder wachtwoord. In
                            plaats van een wachtwoord gebruik je iets wat al op je apparaat staat, zoals je
                            vingerafdruk, gezichtsherkenning of een fysieke sleutel (zoals een Yubikey).</p>
                        <p className="section-label"><strong>Een passkey toevoegen</strong></p>
                        <ol>
                            <li>Ga naar Mijn eduID en log in zoals je dat normaal doet.</li>
                            <li>Kies links voor Beveiliging en klik op Passkey toevoegen.</li>
                            <li>Geef de sleutel een herkenbare naam (bijv. Vingerafdruk iPhone of Gele Yubikey) en
                                klik op Start.
                            </li>
                            <li>Volg de instructies van je browser. Klaar!</li>
                        </ol>
                        <p className="section-label"><strong>Inloggen met een passkey</strong></p>
                        <ol>
                            <li>Ga naar de dienst waarvoor je wilt inloggen met eduID.</li>
                            <li>Kies op het inlogscherm voor Login met een passkey.</li>
                            <li>Vul je e-mailadres in en volg de instructies van je browser. Klaar!</li>
                        </ol>
                        <p>Let op: een passkey die je instelt in één browser (bijvoorbeeld Chrome) werkt niet
                            automatisch in een andere browser (bijvoorbeeld Safari). Voeg de passkey in die browser
                            opnieuw toe.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>eduID app</h2>
                    <InfoLinkField title="Ik ontvang de sms-code niet.">
                        <ul>
                            <li>Heb je vandaag vaak een sms-code aangevraagd? Dan zit je mogelijk aan de limiet.
                                Probeer het na 24 uur opnieuw.
                            </li>
                            <li>Gebruik altijd de laatst ontvangen sms-code.</li>
                            <li>Lukt het nog steeds niet? Neem contact op via <a
                                href="mailto:help@eduid.nl">help@eduid.nl</a>.
                            </li>
                        </ul>
                    </InfoLinkField>
                    <InfoLinkField title="Ik kan niet meer bij de app of ben mijn pincode vergeten.">
                        <ol>
                            <li>Ga naar eduid.nl en log in. Kies onderaan voor een andere inlogmethode.</li>
                            <li>Kies "Ontvang een login code in je inbox".</li>
                            <li>Je ontvangt een code per mail.</li>
                            <li>Na het inloggen kun je zelf de app deactiveren uit je account. Dit doe je via
                                Beveiliging &rarr; "Deactiveer je eduID mobiele app". Voordat je de app kan
                                deactiveren moet je een herstel code invullen. Deze herstel code krijg je of als
                                SMS, of heb je zelf een keer opgeslagen. Deze herstelcode is anders dan je login
                                code.
                            </li>
                        </ol>
                        <p>Daarna kun je de app opnieuw toevoegen.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Ik krijg de foutmelding 'registratie mislukt'">
                        <p>Volg de onderstaande stappen om dit op te lossen. Zorg dat je stap 1 volledig afrondt
                            voordat je verdergaat met stap 2.</p>
                        <p className="section-label"><strong>Stap 1: Verwijder de app en reset je browser</strong></p>
                        <ul>
                            <li>Verwijder de eduID app van je telefoon.</li>
                            <li>Log in op eduid.nl en verwijder de app van je account.</li>
                            <li>Verwijder de cookies en het (recent) browsergeschiedenis van zowel de browser op je
                                telefoon als op je pc.
                            </li>
                        </ul>
                        <p className="section-label"><strong>Stap 2: Nieuwe installatie</strong></p>
                        <ul>
                            <li>Download de eduID app op je (nieuwe) telefoon.</li>
                            <li>Open een nieuw privé/incognito venster in je browser.</li>
                            <li>Log opnieuw in op eduid.nl.</li>
                            <li>Ga naar 'Beveiliging' en kies 'Start app registratie'.</li>
                            <li>Volg de aanwijzingen op het scherm om de app registratie te voltooien.</li>
                        </ul>
                    </InfoLinkField>
                    <InfoLinkField title="Ik kan geen QR-code scannen">
                        <p>Controleer of de eduID app toestemming heeft om de camera te gebruiken. Dit stel je in
                            bij de app-instellingen op je telefoon.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Welke telefoons worden ondersteund?">
                        <p>De eduID app werkt alleen op:</p>
                        <ul>
                            <li>iOS (versie 13 en hoger)</li>
                            <li>Android (officiële Android-versies)</li>
                        </ul>
                        <p>Andere besturingssystemen worden niet ondersteund.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom">
                    <h2>Meer hulp nodig?</h2>
                    <p className="info">Staat je vraag er niet tussen? Stuur dan een e-mail naar <a
                        href="mailto:help@eduid.nl">help@eduid.nl</a>. We helpen je graag en leggen je uit hoe het werkt.</p>
                </div>
            </Background>
        </div>
    );
}
