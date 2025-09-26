import {CollapseField} from "../components/CollapseField.jsx";
import {Background} from "../components/Background.jsx";
import {AnchorLink} from "../components/AnchorLink.jsx";
import {Link} from "react-router";
import {useAppStore} from "../stores/AppStore.js";

export const PrivacyEN = () => {

    const config = useAppStore((state) => state.config);

    return (
        <Background>
            <div className="card full">
                <p className="info">
                    <strong>Please note:</strong> the Dutch version of the Privacy Policy is authoritative.<br/>
                    Thank you for viewing the privacy policy for eduID! SURF’s eduID team pays a lot of attention to the
                    protection of your personal data and you can read all about it in this privacy policy. If you have
                    any
                    questions or concerns about this privacy policy, please email <a
                    href="mailto:help@eduid.nl">help@eduid.nl</a>.
                </p>
                <p className="info">
                    This privacy policy contains the following topics:
                    <ol>
                        <li><AnchorLink identifier="eduid" label="What is eduID?"/></li>
                        <li><AnchorLink identifier="surf" label="Contact details SURF"/></li>
                        <li><AnchorLink identifier="gegevens" label="What data do we process from you?"/></li>
                        <li><AnchorLink identifier="persoonsgegevens"
                                        label="Why is eduID allowed to process your personal data?"/></li>
                        <li><AnchorLink identifier="verstrekken" label="To whom do we provide your data?"/></li>
                        <li><AnchorLink identifier="waar" label="Where do we store your data?"/></li>
                        <li><AnchorLink identifier="bewaren" label="How long do we keep your data?"/></li>
                        <li><AnchorLink identifier="rechten" label="What rights do you have?"/></li>
                        <li><AnchorLink identifier="terecht" label="Where can you go to exercise your rights?"/>
                        </li>
                        <li><AnchorLink identifier="cookies" label="Which cookies does eduID use?"/></li>
                        <li><AnchorLink identifier="privacyverklaring" label="Changes to privacy policy"/></li>
                    </ol>
                </p>

                <CollapseField title="Changes since previous versions">
                    <ul>
                        <li>Changes in version 18 October 2024
                            <ul className="inner">
                                <li>Added the data which is processed when verifying the eduID via iDIN or eIDAS.</li>
                            </ul>
                        </li>
                        <li>Changes in version 17 June 2024
                            <ul className="inner">
                                <li>Large parts rewritten. The most important changes are:</li>
                                <li>Purposes and lawfulness of processing. It is also indicated for which processing
                                    SURF is the controller and for which processing an institution.
                                </li>
                                <li>Retention periods expanded and explained why the retention period is used.</li>
                                <li>Your rights and where you can exercise them are better described.</li>
                            </ul>
                        </li>
                        <li>Changes in version 4 april 2023
                            <ul className="inner">
                                <li>Cookie overview updated.</li>
                            </ul>
                        </li>
                        <li>Changes in version 16 January 2023
                            <ul className="inner">
                                <li>Added data processing of the eduID app.</li>
                                <li>Cookie overview updated</li>
                            </ul>
                        </li>
                        <li>Changes in version 29 September 2020
                            <ul className="inner">
                                <li>We changed the email address mentioned in this privacy policy from
                                    help@surfconext.nl to help@eduid.nl.
                                </li>
                                <li>The privacy policy now explains which cookies eduID uses. eduID does not use any
                                    tracking or analytical cookies.
                                </li>
                                <li>eduID bevat nu de mogelijkheid om andere accounts te linken.</li>
                                <li>eduID now contains the feature to connect other accounts to eduID. The privacy
                                    policy desribes which (extra) data is being stored when you connect another account
                                    to your eduID.
                                </li>
                                <li>Several smaller textual changes to improve readability.</li>
                            </ul>
                        </li>
                    </ul>
                </CollapseField>


            </div>
            <div id="eduid" className="card full bottom">
                <h5>What is eduID?</h5>
                <p className="info">
                    An eduID account is a digital identity of the user offered and managed by SURF, which can be used in
                    the
                    field of education and research. Any person can create an eduID account, regardless of whether this
                    person
                    is affiliated with an institution. So not only students, but also internship or practice
                    supervisors,
                    (guest) lecturers, researchers, alumni, pre-registrants, professionals, people from the business
                    community
                    and others. With this account the user can log in to applications connected to eduID. These
                    applications
                    can be from institutions affiliated with SURF, from SURF itself or from third parties. <br/><br/>
                    <Link to={"/about"}>Read more about eduID.</Link>
                </p>
            </div>
            <div id="surf" className="card full bottom">
                <h5>Contact details SURF</h5>
                <p className="info">
                    eduID is offered and managed by SURF, a cooperative of Dutch educational and research
                    institutions.<br/><br/>
                    SURF<br/>
                    Moreelsepark 48<br/>
                    3511 EP Utrecht, Nederland<br/>
                    <a href="https://www.surf.nl">www.surf.nl</a>
                </p>
                <p>The contact details of our data protection officer are: <a href="mailto:fg@surf.nl">fg@surf.nl</a>
                </p>
            </div>
            <div id="gegevens" className="card full bottom">
                <h5>What data do we process from you?</h5>
                <p className="info">
                    <ol>
                        <li>eduID processes personal data of the natural person who is the holder of an eduID. This
                            concerns the
                            following data:
                            <ul>
                                <li>Your e-mailaddress</li>
                                <li>Your first and last name</li>
                                <li>A unique identifying number and associated pseudonyms provided to services</li>
                                <li>The date and time when the first login takes place for each service to which you log
                                    in with eduID
                                </li>
                                <li>Preferred language eduID interface</li>
                                <li>Browser used (name, version, OS, device type)</li>
                                <li>IP address</li>
                                <li>Temporary Session ID</li>
                            </ul>
                        </li>
                        <br/>

                        <li>If you link your eduID to your institution&apos;s account, eduID also processes the
                            following data:
                            <ul>
                                <li>Name of the associated institution</li>
                                <li>Your first and last name as known to the linked institution&nbsp;</li>
                                <li>Username of your account at the linked institution</li>
                                <li>Your role(s) within the linked institution (for example student or employee)</li>
                            </ul>
                        </li>
                        <br/>

                        <li>If you verify your eduID using iDIN, eduID also processes the following data:
                            <ul>
                                <li>Your first name and surname as known to the bank through which the verification is
                                    carried
                                    out.
                                </li>
                                <li>Your date of birth as known by the bank through which verification is carried out.
                                </li>
                            </ul>
                        </li>
                        <br/>

                        <li>If you verify your eduID through <a href="https://www.logius.nl/domeinen/toegang/eidas/wat-is-het">eID</a>
                            (eIDAS), eduID also processes the following data:
                            <ul>
                                <li>Your first name and last name as known to the eID through which the verification is
                                    carried
                                    out.
                                </li>
                                <li>Your date of birth as known to the eID with which verification is performed.</li>
                                <li>A PseudoID, an identifying number used to identify a natural person</li>
                            </ul>
                        </li>
                        <br/>

                        <li>If you use the eduID app, eduID also processes the following data:
                            <ul>
                                <li>A unique identifying number from your eduID app registration</li>
                                <li>A unique identifying number from your phone to send a push message</li>
                                <li>Optional: your mobile phone number if you choose your phone number as the recovery
                                    method
                                </li>
                            </ul>
                        </li>
                    </ol>
                </p>
            </div>
            <div id="persoonsgegevens" className="card full bottom">
                <h5>Why is eduID allowed to process your personal data?</h5>
                <p className="info">
                    Personal data may only be processed if there is a legal basis for this. The basis differs per
                    purpose and
                    who is the controller.<br/>
                    SURF is the controller for several purposes. SURF ensures a lawful basis for processing personal
                    data:
                    <ul>
                        <li>Purpose: when you create an eduID, you add several personal data such as name and email
                            address.
                            These are used by eduID and applications to which you log in to recognize you and
                            communicate with
                            you via email. <br/>
                            Lawfulness: execution of an agreement, namely the agreement between SURF and the person who
                            creates
                            an eduID. When creating, the user is shown the eduID terms of use and by agreeing to them,
                            the eduID
                            is created.
                        </li>
                        <li>Purpose: to log in to an application with eduID where personal data can be provided to the
                            application
                            to recognize you, provide you with authorization or communicate with you via e-mail. This
                            purpose
                            applies to applications that are not prescribed or required by an institution with which you
                            have a
                            relationship. <br/>
                            Lawfulness: execution of an agreement, namely the terms of use that you accept when creating
                            your eduID.
                        </li>
                        <li>Purpose: To provide you with insight into your login history, we keep track of which
                            application you have
                            logged in to and what personal data has been provided to the application. <br/>
                            Lawfulness: execution of an agreement, namely the terms of use that you accept when creating
                            your eduID.
                        </li>
                        <li>Purpose: We process the technical data mentioned for the correct operation of eduID. <br/>
                            Lawfulness: execution of an agreement, namely the terms of use that you accept when creating
                            your eduID.
                        </li>
                    </ul>
                </p>
                <p className="info">
                    An institution is controller for several purposes. The institution ensures a lawful basis for
                    processing
                    personal data:
                    <ul>
                        <li>Purpose: to log in to an application with eduID where personal data can be provided to the
                            application
                            in order to recognize you, provide you with authorization or communicate with you via
                            e-mail. This
                            purpose applies to applications for which the institution requires logging in with
                            eduID <br/>
                            Lawfulness: this is determined by the institution, and can be, for example: performing a
                            task of public
                            interest such as education, or the performance of a contract, such as an apprenticeship
                            agreement,
                            contract education or employment contract.
                        </li>
                        <li>Purpose: some applications require more and/or reliable personal data before access can be
                            granted.
                            You can add this data to your eduID by linking your eduID to an external data source, such
                            as an
                            educational institution, bank or national eID. For example your name as known to the
                            institution, your
                            relationship with the institution (e.g. student or employee) and the organization name.
                            Providing these
                            personal data to eduID is done under the responsibility of the institution. <br/>
                            Lawfulness: this is determined by the institution, and can be, for example: performing a
                            task of public
                            interest such as education, or the performance of a contract, such as an apprenticeship
                            agreement,
                            contract education or employment contract. <br/>
                            It is good to know that this data can subsequently be released under the responsibility of
                            SURF when
                            logging in to an application (see above).
                        </li>
                    </ul>
                </p>
            </div>
            <div id="verstrekken" className="card full bottom">
                <h5>To whom do we provide your data?</h5>
                <p className="info">
                    eduID only provides your personal data to third parties if this is necessary for you to access the
                    application.
                    For example, eduID provides data to applications to which you log in via eduID. The first time you
                    log in to an
                    application with eduID, you will see an information screen showing exactly what data is provided to
                    the
                    application. Your data will only be transferred if you agree to this. By closing this window, you
                    can prevent
                    the application from receiving your data. You cannot log in to the application with eduID.
                </p>
                <p>
                    Via <a href={config.spBaseUrl}>My eduID</a> you can see which services you have logged into with
                    eduID.
                </p>
                <p>
                    We will only provide your data to parties other than the above with your permission unless it is
                    legally
                    required or permitted to provide your data. For example, the police can request data from us in the
                    context
                    of a fraud investigation. SURF is then legally obliged to provide this information.
                </p>
                <p> There are also various parties involved in offering the platform. The following party processes
                    personal data
                    on the instructions and instructions of SURF:
                    <ul>
                        <li>Hosting and management provider</li>
                        <li>Identitybroker for iDIN and eIDAS</li>
                    </ul>
                </p>
            </div>
            <div id="waar" className="card full bottom">
                <h5>Where do we store your data?</h5>
                <p className="info">
                    The eduID infrastructure is hosted on SURF infrastructure. Its servers are located in Amsterdam and
                    Utrecht,
                    with a backup location in Tilburg.
                </p>
            </div>
            <div id="bewaren" className="card full bottom">
                <h5>How long do we keep your data?</h5>
                <p className="info">
                    The personal data obtained from an institution by linking your eduID to an institutional account
                    will be kept
                    for 6 months, except for the obtained first and/or last name, which will be kept for 6 years. This
                    period has
                    been chosen to keep this data sufficiently up to date.
                </p>
                <p className="info">
                    The retention period for all eduID account data is 5 years after the last time you log in somewhere
                    with your
                    eduID. This period has been chosen because in the process of lifelong development it is expected
                    that there will
                    be periods in which a user does not use his eduID, but that the eduID can become relevant to that
                    person again.
                    In the meantime, eduID will send reminders if the account is in danger of being removed.
                </p>
                <p className="info">
                    The technical log data is kept for six months to allow time to investigate any problems and
                    incidents.
                </p>
            </div>
            <div id="rechten" className="card full bottom">
                <h5>What rights do you have?</h5>
                <p className="info">
                    You have the right to have the personal data that eduID processes about you changed, supplemented,
                    or deleted.
                    You can also request access to the personal data that is processed about you. You can view the
                    information that
                    eduID has about you on <a href={config.spBaseUrl}>My eduID</a>. You can also change or supplement
                    your details
                    there.
                </p>
                <p className="info">
                    If it concerns automatic processing of data provided by you based on consent or the execution of an
                    agreement,
                    you can request an overview in a structured and common form of the personal data that we process
                    about you via <a href={config.spBaseUrl}>Mijn eduID</a>. You also have the right to have this data transferred to
                    another
                    party, provided this is technically possible.
                </p>
                <p className="info">
                    You can also submit a request to restrict the processing of your personal data, which will cause the
                    controller
                    to temporarily stop processing your data. This happens if:
                    <ul>
                        <li>you object (see further explanation below), or</li>
                        <li>you contest the accuracy of personal data being processed, or</li>
                        <li>you believe that the processing of data is unlawful, or</li>
                        <li>you believe that the controller no longer needs your personal data, but you need them in the
                            context
                            of a legal claim.
                        </li>
                    </ul>
                </p>
                <p className="info">
                    <strong>NB</strong> If eduID restricts the processing of data necessary for running our services,
                    this
                    restriction may affect the functioning of the service.
                </p>
                <p className="info">
                    <strong>Right to object</strong><br/>
                    You can object to the processing of your personal data if your data is processed based on a
                    legitimate interest
                    or on the basis of the performance of a task of public interest. If the controller has no compelling
                    legitimate
                    grounds to continue the processing, the processing will cease.
                </p>
                <p className="info">
                    If you object, you can also submit a request to restrict the processing of your personal data during
                    this
                    objection.
                </p>
                <p className="info">
                    <strong>How to file a complaint</strong><br/>
                    If you believe that eduID is not handling your personal data properly, you can file a complaint with
                    the data
                    protection officer of SURF or an institution. You also have the right to file a complaint with the
                    Dutch Data
                    Protection Authority. More information about the Dutch Data Protection Authority and submitting
                    complaints can be
                    found at <a href="https://www.autoriteitpersoonsgegevens.nl">www.autoriteitpersoonsgegevens.nl</a>.
                </p>
            </div>
            <div id="terecht" className="card full bottom">
                <h5>Where can you go to exercise your rights?</h5>
                <p className="info">
                    You can submit a request to exercise your rights to the organization responsible for processing your
                    personal
                    data (the controller). However, for eduID these can be different organizations: SURF or one of the
                    participating
                    institutions. SURF coordinates the requests and puts you in touch with the right person at the right
                    institution.
                    Please contact us at: <a href="mailto:help@eduid.nl">help@eduid.nl</a>. You can of course also contact the relevant institution
                    directly.
                </p>
            </div>
            <div id="cookies" className="card full bottom">
                <h5>Which cookies does eduID use?</h5>
                <p className="info">
                    eduID places cookies on the device you use to visit eduID. Cookies are small files that are sent by
                    an internet
                    server and stored on your device. The cookies that eduID places are necessary for the functioning of
                    eduID. eduID
                    does not place analytical or tracking cookies.
                </p>
                <p className="info">
                    <strong>Functional cookies</strong><br/>
                    eduID uses several functional cookies that ensure that eduID functions correctly:
                    <ul>
                        <li>Cookie ‘login_preference’ to remember how you log in (e.g. via magic link or password). This
                            cookie is
                            valid for 1 year.
                        </li>
                        <li>Cookie ‘lang’ to remember in which language you wish to see the eduID interface. This cookie
                            is valid for
                            1 year.
                        </li>
                        <li>Cookie ‘REGISTER_MODUS’ to indicate whether you should proceed with the registration
                            process. This cookie
                            is only set during the session and then deleted.
                        </li>
                        <li>Cookie ‘BROWSER_SESSION’ to ensure that you use the magic link in the same browser where the
                            login was
                            initiated. This cookie is only set during the session and then deleted.
                        </li>
                        <li>Cookie ‘guest-idp-remember-me’, to enable you to remain logged in (in the browser where you
                            use this).
                            This cookie is valid for 6 months.
                        </li>
                        <li>Cookie ‘username’ to remember your username so that you do not have to enter it next time.
                        </li>
                        <li>Cookie ‘REMEMBER_ME_QUESTION_ASKED_COOKIE’ to remember whether eduID has asked that you
                            remain logged in.
                        </li>
                        <li>Cookie ‘TIQR_COOKIE’ to remember whether you have already logged in with the eduID app in
                            this browser
                        </li>
                        <li>Cookie ‘TRACKING_DEVICE’ to detect whether this is a new device that is being logged in. If
                            this is a new
                            device, a notification email will be sent.
                        </li>
                    </ul>
                </p>
            </div>
            <div id="privacyverklaring" className="card full bottom">
                <h5>Changes to privacy policy</h5>
                <p className="info">
                    Changes may be made to this privacy policy. We therefore recommend that you consult this privacy
                    policy
                    regularly. The version number is at the top of the page.
                </p>
            </div>
        </Background>
    );
}
