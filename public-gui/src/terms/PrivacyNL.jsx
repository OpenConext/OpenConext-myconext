import {CollapseField} from "../components/CollapseField.jsx";
import {Background} from "../components/Background.jsx";
import {AnchorLink} from "../components/AnchorLink.jsx";
import {Link} from "react-router";
import {useAppStore} from "../stores/AppStore.js";

export const PrivacyNL = () => {

    const config = useAppStore((state) => state.config);

    return (
        <Background>
            <div className="card">
                <p className="info">
                    Goed dat je de privacyverklaring voor eduID bekijkt! Het eduID-team van SURF besteedt veel aandacht
                    aan de bescherming van je persoonsgegevens en in deze privacyverklaring lees je daar alles over. Als
                    je vragen of zorgen hebt over deze privacyverklaring, mail dan gerust naar <a
                    href="mailto:help@eduid.nl">help@eduid.nl</a>.
                </p>
                <p className="info">
                    Deze privacyverklaring bevat de volgende onderwerpen:
                    <ol>
                        <li><AnchorLink identifier="eduid" label="Wat is eduID?"/></li>
                        <li><AnchorLink identifier="surf" label="Contactgegevens SURF"/></li>
                        <li><AnchorLink identifier="gegevens" label="Welke gegevens verwerken wij van jou?"/></li>
                        <li><AnchorLink identifier="persoonsgegevens"
                                        label="Waarom mag eduID je persoonsgegevens verwerken??"/></li>
                        <li><AnchorLink identifier="verstrekken" label="Aan wie verstrekken wij je gegevens?"/></li>
                        <li><AnchorLink identifier="waar" label="Waar slaan wij je gegevens op??"/></li>
                        <li><AnchorLink identifier="bewaren" label="Hoe lang bewaren wij je gegevens?"/></li>
                        <li><AnchorLink identifier="rechten" label="Welke rechten heb je?"/></li>
                        <li><AnchorLink identifier="terecht" label="Waar kun je terecht om je rechten uit te oefenen?"/>
                        </li>
                        <li><AnchorLink identifier="cookies" label="Welke cookies plaatst eduID?"/></li>
                        <li><AnchorLink identifier="privacyverklaring" label="Wijzigingen privacyverklaring"/></li>
                    </ol>
                </p>
                <CollapseField title="Wijzigingshistorie"
                               info="Versie 1: 17 juni 2024"/>

            </div>
            <div id="eduid" className="card bottom">
                <h5>Wat is eduID?</h5>
                <p className="info">
                    Een eduID-account is een door SURF aangeboden en beheerde digitale identiteit van de gebruiker, die
                    gebruikt kan worden in het domein van onderwijs en onderzoek. Iedere persoon kan een eduID-account
                    aanmaken, ongeacht of deze persoon verbonden is aan een instelling. Dus niet alleen studenten, maar
                    bijvoorbeeld ook stage- of praktijkbegeleiders, (gast)docenten, onderzoekers, alumni,
                    voorinschrijvers, professionals, mensen uit het bedrijfsleven en anderen. Met dit account kan de
                    gebruiker inloggen op aan eduID gekoppelde applicaties. Deze applicaties kunnen van instellingen
                    zijn die bij SURF zijn aangesloten, van SURF zelf of van derden.<br/>
                    <Link to={"/about"}>Lees meer over eduID.</Link>
                </p>
            </div>
            <div id="surf" className="card bottom">
                <h5>Contactgegevens SURF</h5>
                <p className="info">
                    eduID wordt aangeboden en beheerd door SURF, een coöperatie van Nederlandse onderwijs- en
                    onderzoeksinstellingen.<br/>
                    SURF<br/>
                    Moreelsepark 48<br/>
                    3511 EP Utrecht, Nederland<br/>
                    <a href="www.surf.nl">www.surf.nl</a>
                </p>
                <p>De contactgegevens van onze functionaris
                    gegevensbescherming zijn: <a href="mailto:fg@surf.nl">fg@surf.nl</a></p>
            </div>
            <div id="gegevens" className="card bottom">
                <h5>Welke gegevens verwerken wij van jou?</h5>
                <p className="info">
                    eduID verwerkt persoonsgegevens van de natuurlijke persoon die houder is van een eduID. Het gaat om
                    de volgende gegevens:
                    <ol>
                        <li>Je e-mailadres</li>
                        <li>Je voor- en achternaam</li>
                        <li>Een uniek identificerend nummer en daarbij behorende pseudoniemen die aan diensten worden
                            verstrekt
                        </li>
                        <li>De datum en het tijdstip waarop de eerste login plaatsvindt van iedere dienst waarop je met
                            eduID inlogt
                        </li>
                        <li>Extra technische data:
                            <ul>
                                <li>Voorkeurstaal eduID-interface</li>
                                <li>Technische logging (user agent)</li>
                                <li>IP-adres</li>
                                <li>Tijdelijk Session ID</li>
                            </ul>
                        </li>
                    </ol>
                </p>
                <p>
                    Als je je eduID koppelt aan het account van je instelling, dan verwerkt eduID ook de volgende
                    gegevens:
                    <ol>
                        <li>Naam van de gekoppelde instelling</li>
                        <li>Je voor- en achternaam zoals bekend bij de gekoppelde instelling</li>
                        <li>Gebruikersnaam van je account bij de gekoppelde instelling</li>
                        <li>Je rol(len) binnen de gekoppelde instelling (bijvoorbeeld ‘student’ of ‘medewerker’)
                        </li>
                    </ol>
                </p>
                <p>
                    Als je de eduID-app gebruikt, dan verwerkt eduID ook de volgende gegevens:
                    <ol>
                        <li>Een uniek identificerend nummer van je eduID-app-registratie</li>
                        <li>Een uniek identificerend nummer van je telefoon om een push-bericht te kunnen sturen
                        </li>
                        <li>Optioneel: je mobiele telefoonnummer als je als herstelmethode je telefoonnummer hebt
                            gekozen
                        </li>
                    </ol>
                </p>
            </div>
            <div id="persoonsgegevens" className="card bottom">
                <h5>Waarom mag eduID je persoonsgegevens verwerken?</h5>
                <p className="info">
                    Persoonsgegevens mogen enkel worden verwerkt als hier een wettelijke grondslag voor is. De grondslag
                    verschilt per doel en wie verwerkingsverantwoordelijke is. Voor een aantal doelen is SURF de
                    verwerkingsverantwoordelijke. SURF zorgt voor een rechtmatige grondslag voor verwerking van
                    persoonsgegevens:
                    <ul>
                        <li>Doel: als je een eduID aanmaakt, voeg je hier zelf een aantal persoonsgegevens aan toe zoals
                            naam en
                            e-mailadres. Deze worden gebruikt door eduID en applicaties waarop je inlogt, om jou te
                            kunnen
                            herkennen en met jou te communiceren via e-mail. Grondslag: uitvoering van een overeenkomst,
                            namelijk de overeenkomst tussen SURF en degene die een eduID aanmaakt. Bij het aanmaken
                            krijgt de
                            gebruiker de eduID-gebruiksvoorwaarden te zien en door daarmee akkoord te gaan, wordt het
                            eduID
                            aangemaakt.
                        </li>
                        <li>Doel: met eduID inloggen op een applicatie waarbij persoonsgegevens verstrekt kunnen worden
                            aan de
                            applicatie om jou te kunnen herkennen, je te voorzien van een autorisatie of met je te
                            communiceren
                            via e-mail. Dit doel geldt voor applicaties die niet voorgeschreven of verplicht worden door
                            een
                            instelling waar je een relatie mee hebt. Grondslag: uitvoering van een overeenkomst,
                            namelijk de
                            gebruiksvoorwaarden die je accepteert bij het aanmaken van je eduID.
                        </li>
                        <li>Doel: Om je inzicht te kunnen geven in je login-geschiedenis, houden we bij op welke
                            applicatie je
                            hebt ingelogd, en welke persoonsgegevens daarbij aan de applicatie zijn verstrekt.
                            Grondslag:
                            uitvoering van een overeenkomst, namelijk de gebruiksvoorwaarden die je accepteert bij het
                            aanmaken
                            van je eduID.
                        </li>
                        <li>Doel: voor de juiste werking van eduID verwerken we genoemde technische data. Grondslag:
                            uitvoering
                            van een overeenkomst, namelijk de gebruiksvoorwaarden die je accepteert bij het aanmaken van
                            je
                            eduID.
                        </li>
                    </ul>
                </p>
                <p className="info">
                    Voor een aantal doelen is een instelling verwerkingsverantwoordelijke. De instelling zorgt voor een
                    rechtmatige grondslag voor verwerking van persoonsgegevens:
                    <ul>
                        <li>Doel: met eduID inloggen op een applicatie waarbij persoonsgegevens verstrekt kunnen worden
                            aan de
                            applicatie om jou te kunnen herkennen, je te voorzien van een autorisatie of met je te
                            communiceren
                            via e-mail. Dit doel geldt voor applicaties waarvan de instelling het inloggen met eduID
                            verplicht
                            stelt Grondslag: deze wordt bepaald door de instelling, en kan bijvoorbeeld zijn: het
                            vervullen van
                            een taak van algemeen belang zoals onderwijs, of uitvoering van een overeenkomst zijn, zoals
                            een
                            leerovereenkomst, contractonderwijs of arbeidscontract.
                        </li>
                        <li>Doel: sommige applicaties hebben meer en/of betrouwbare persoonsgegevens nodig voordat
                            toegang
                            verleend kan worden. Deze gegevens kun je aan je eduID toevoegen door je eduID te koppelen
                            aan een
                            externe gegevensbron, zoals een onderwijsinstelling. Denk dan aan je naam zoals bekend bij
                            de
                            instelling, je relatie met de instelling (bijv. student of medewerker) en de
                            organisatienaam. Het
                            verstrekken van deze persoonsgegevens aan eduID gebeurt onder verantwoordelijkheid van de
                            instelling.
                        </li>
                        <li>Grondslag: deze wordt bepaald door de instelling, en kan bijvoorbeeld zijn: het vervullen
                            van een
                            taak van algemeen belang zoals het verzorgen van onderwijs , of uitvoering van een
                            overeenkomst,
                            zoals een leerovereenkomst, contractonderwijs of arbeidscontract.
                        </li>
                    </ul>
                </p>
                <p className="info">
                    Het is goed om te weten dat deze gegevens vervolgens weer onder verantwoordelijkheid van SURF
                    vrijgegeven kunnen worden tijdens het inloggen op een applicatie (zie hierboven).
                </p>
            </div>
            <div id="verstrekken" className="card bottom">
                <h5>Aan wie verstrekken wij je gegevens?</h5>
                <p className="info">
                    eduID verstrekt je persoonsgegevens alleen aan derde partijen als dit noodzakelijk is om de
                    betreffende applicatie aan jou te kunnen leveren. Zo verstrekt eduID gegevens aan applicaties waarop
                    je inlogt via eduID. Bij de eerste keer dat je met eduID inlogt op een applicatie, krijg je een
                    informatiescherm te zien met welke gegevens er precies aan de applicatie worden verstrekt. Pas als
                    je hier akkoord geeft, worden je gegevens daadwerkelijk doorgegeven. Door dit scherm te sluiten kun
                    je voorkomen dat de applicatie je gegevens ontvangt. Je kunt dan ook niet met eduID inloggen op de
                    applicatie.
                </p>
                <p>
                    Via <a href={config.spBaseUrl}>Mijn eduID</a> kun je zien welke diensten je via eduID hebt gebruikt.
                    Aan andere partijen dan bovenstaande verstrekken we je gegevens alléén na jouw toestemming, tenzij
                    het wettelijk verplicht of toegestaan is je gegevens te verstrekken. Zo kan bijvoorbeeld de politie
                    in het kader van een fraudeonderzoek gegevens bij ons opvragen. SURF is dan wettelijk verplicht deze
                    gegevens af te geven.
                </p>
                <p> Er zijn daarnaast verschillende partijen betrokken bij het aanbieden van het platform. De volgende
                    partij verwerkt in opdracht en instructie van SURF persoonsgegevens:
                    <ul>
                        <li>Hosting- en beheerpartij</li>
                    </ul>
                </p>
            </div>
            <div id="waar" className="card bottom">
                <h5>Waar slaan wij je gegevens op?</h5>
                <p className="info">
                    De eduID-infrastructuur wordt gehost op infrastructuur van SURF. De servers daarvan bevinden zich in
                    Amsterdam en Utrecht, met een back-uplocatie in Tilburg.
                </p>
            </div>
            <div id="bewaren" className="card bottom">
                <h5>Hoe lang bewaren wij je gegevens?</h5>
                <p className="info">
                    De persoonsgegevens die verkregen zijn van een instelling door je eduID te koppelen aan een
                    instellingsaccount worden 6 maanden bewaard, met uitzondering van de verkregen voor- en/of
                    achternaam, die worden 6 jaar bewaard. Deze periode is gekozen om deze gegevens nog voldoende
                    up-to-date te kunnen houden.
                </p>
                <p className="info">
                    De bewaartermijn voor alle eduID accountgegevens is 5 jaar na de laatste keer dat je met je eduID
                    ergens inlogt. Deze periode is gekozen omdat in het proces van leven-lang ontwikkelen het te
                    verwachten is dat er periodes zijn waarin een gebruiker zijn eduID niet gebruikt, maar dat het eduID
                    daarna wel weer relevant wordt voor die persoon. In die tussentijd zal eduID reminders sturen als
                    het account dreigt te verdwijnen.
                </p>
                <p className="info">
                    De technische loggegevens worden zes maanden bewaard om nog tijd te hebben om eventuele problemen en
                    incidenten te kunnen onderzoeken.
                </p>
            </div>
            <div id="rechten" className="card bottom">
                <h5>Welke rechten heb je?</h5>
                <p className="info">
                    Je hebt het recht om de persoonsgegevens die eduID van jou verwerkt te laten wijzigen, aanvullen of
                    verwijderen. Ook kun je inzage verzoeken in de persoonsgegevens die van je worden verwerkt. De
                    gegevens die eduID van jou heeft kun je inzien op <a href={config.spBaseUrl}>Mijn eduID</a>.
                    Daar kun je je gegevens ook wijzigen
                    of aanvullen.
                </p>
                <p className="info">
                    Als het gaat om een automatische verwerking van door jou zelf verstrekte gegevens op basis van
                    toestemming of de uitvoering van een overeenkomst, kun je een overzicht in een gestructureerde en
                    gangbare vorm opvragen van de persoonsgegevens die wij van jou verwerken via <a
                    href={config.spBaseUrl}>Mijn eduID</a>. Ook heb je
                    het recht op overdraagbaarheid van deze gegevens naar een andere partij, mits dit technisch mogelijk
                    is.
                </p>
                <p className="info">
                    Je kunt ook een verzoek indienen om de verwerking van je persoonsgegevens te beperken, waardoor de
                    verwerkingsverantwoordelijke tijdelijk ophoudt met het verwerken van je gegevens. Dit gebeurt als:
                    <ul>
                        <li>je bezwaar maakt (zie verderop meer uitleg), of</li>
                        <li>je de juistheid van persoonsgegevens die worden verwerkt, betwist, of</li>
                        <li>je vindt dat de verwerking van gegevens onrechtmatig is, of</li>
                        <li>je vindt dat de verwerkingsverantwoordelijke je persoonsgegevens niet meer nodig heeft, maar
                            jij ze
                            nodig hebt in het kader van een rechtsvordering.
                        </li>
                    </ul>
                </p>
                <p className="info">
                    <strong>Let op!</strong> Als eduID de verwerking beperkt van gegevens die nodig zijn voor het
                    verlenen van onze
                    dienstverlening, kan deze beperking invloed hebben op het functioneren van de dienst.
                </p>
                <p className="info">
                    <strong>Recht van bezwaar</strong><br/>
                    Je kunt bezwaar maken tegen verwerking van je persoonsgegevens als je gegevens verwerkt worden op
                    basis van een gerechtvaardigd belang of op basis van de uitvoering van een taak van algemeen belang.
                    Als de verwerkingsverantwoordelijke geen dwingende gerechtvaardigde gronden heeft om de verwerking
                    voort te zetten, zal de verwerking worden gestaakt.
                </p>
                <p className="info">
                    Als je bezwaar maakt, kun je ook een verzoek indienen om de verwerking van je persoonsgegevens te
                    beperken gedurende dit bezwaar.
                </p>
                <p className="info">
                    <strong>Klacht indienen</strong><br/>
                    Als je vindt dat eduID niet goed omgaat met jouw persoonsgegevens kun je een klacht indienen bij de
                    functionaris gegevensbescherming van SURF of van een instelling. Ook heb je het recht om een klacht
                    in te dienen bij de Autoriteit Persoonsgegevens. Meer informatie over de Autoriteit Persoonsgegevens
                    en het indienen van klachten vind je op <a
                    href="www.autoriteitpersoonsgegevens.nl">www.autoriteitpersoonsgegevens.nl</a>.
                </p>
            </div>
            <div id="terecht" className="card bottom">
                <h5>Waar kun je terecht om je rechten uit te oefenen?</h5>
                <p className="info">
                    Een verzoek om je rechten uit te oefenen kun je indienen bij de organisatie die verantwoordelijk is
                    voor de verwerking van jouw persoonsgegevens. Voor eduID kunnen dit echter verschillende
                    organisaties zijn: SURF of een van de deelnemende instellingen. SURF coördineert de verzoeken en
                    brengt je in contact met de juiste persoon bij de juiste instelling. Neem hiervoor contact op via:
                    <a href="help@eduid.nl">help@eduid.nl</a>. Je mag uiteraard ook direct contact opnemen met de
                    betreffende instelling.
                </p>
            </div>
            <div id="cookies" className="card bottom">
                <h5>Welke cookies plaatst eduID?</h5>
                <p className="info">
                    eduID plaatst cookies op het apparaat waar je eduID mee bezoekt. Cookies zijn kleine bestanden die
                    verstuurd worden door een internetserver en die worden opgeslagen op je apparaat. De cookies die
                    eduID plaatst zijn noodzakelijk voor het functioneren van eduID. eduID plaatst geen analytische of
                    tracking cookies.
                </p>
                <p className="info">
                    <strong>Functionele cookies</strong><br/>
                    eduID maakt gebruik van een aantal functionele cookies die ervoor zorgen dat eduID correct
                    functioneert:
                    <ul>
                        <li>Cookie ‘login_preference’ om te onthouden op welke manier je inlogt (bijv. via magische link
                            of wachtwoord). Deze cookie is 1 jaar geldig.
                        </li>
                        <li>Cookie ‘lang’ om te onthouden in welke taal je de eduID interface wenst te zien. Deze cookie
                            is 1 jaar geldig.
                        </li>
                        <li>Cookie ‘REGISTER_MODUS’ om aan te geven of je in het registratieproces dient uit te komen.
                            Deze cookie wordt alleen tijdens de sessie gezet, daarna verwijderd.
                        </li>
                        <li>Cookie ‘BROWSER_SESSION’ om ervoor te zorgen dat je de magische link gebruikt in dezelfde
                            browser als waar de login is gestart. Deze cookie wordt alleen tijdens de sessie gezet,
                            daarna verwijderd.
                        </li>
                        <li>Cookie ‘guest-idp-remember-me’, om het mogelijk te maken dat je ingelogd blijft (in de
                            browser waar je dit gebruikt). Deze cookie is 6 maanden geldig.
                        </li>
                        <li>Cookie ‘username’ om je gebruikersnaam te onthouden zodat je die de volgende keer niet hoeft
                            in te vullen.
                        </li>
                        <li>Cookie ‘REMEMBER_ME_QUESTION_ASKED_COOKIE’ om te onthouden of eduID heeft gevraagd dat je
                            ingelogd wilt blijven.
                        </li>
                        <li>Cookie ‘TIQR_COOKIE’ om te onthouden of je in deze browser al een keer ingelogd bent met de
                            eduID app
                        </li>
                        <li>Cookie ‘TRACKING_DEVICE’ om te detecteren of dit een nieuw apparaat is waarop ingelogd
                            wordt. Als dit een nieuw apparaat is wordt er een notificatie e-mail gestuurd.
                        </li>
                    </ul>
                </p>
            </div>
            <div id="privacyverklaring" className="card bottom">
                <h5>Wijzigingen privacyverklaring</h5>
                <p className="info">
                    Er kunnen wijzigingen worden aangebracht in deze privacyverklaring. We raden je daarom aan om deze
                    privacyverklaring geregeld te raadplegen. Het versienummer staat bovenaan de pagina.
                </p>
            </div>
        </Background>
    );
}