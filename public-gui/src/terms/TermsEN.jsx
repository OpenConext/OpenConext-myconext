import {CollapseField} from "../components/CollapseField.jsx";
import {Background} from "../components/Background.jsx";
import {Link} from "react-router";

export const TermsEN = () => {

    return (
        <Background>
            <div className="card full">
                <p className="info">
                    <strong>Please note:</strong> the Dutch version of the terms of service is authoritative. <br/><br/>
                    An eduID account is a digital identity of the user offered and managed by SURF, which can be
                    used in the field of education and research. Any person can create an eduID account, regardless
                    of whether this person is affiliated with an institution. With this account the user can log in
                    to applications connected to eduID. These applications can be from institutions affiliated with
                    SURF, from SURF itself or from third parties.<br/>
                    In these terms of use you will find the rules of conduct for the use of eduID and you will also
                    find the rights and obligations that apply when using an eduID account.<br/>
                    By creating an eduID account, the User agrees to these terms of use.
                </p>

                <CollapseField title="Changes since previous versions">
                    <ul>
                        <li>Changes in version 17 juni 2024
                            <ul className="inner">
                                <li>Terms of Use are better structured. Not much has changed in terms of content.</li>
                                <li>Introduction text has been adjusted</li>
                            </ul>
                        </li>
                        <li>Changes in version 11 maart 2020
                            <ul className="inner">
                                <li>Initial version</li>
                            </ul>
                        </li>
                    </ul>
                </CollapseField>

            </div>
            <div className="card bottom full">
                <h5>
                    1. Definitions
                </h5>
                <p className="info">
                    <strong>1.1</strong> Several definitions are used in these Terms of Use. Definitions are
                    capitalized.
                </p>
                <p>
                    <ul className="indented">
                        <li><strong>Service or eduID Service</strong> <br/>
                            the eduID application as offered by SURF with which a User can obtain an eduID Account
                            and log in to applications connected to eduID.
                        </li>
                        <li><strong>eduID Account</strong> <br/>
                            all the data that the eduID Service keeps track of for a User, including the data with
                            which the User can log in to the eduID Service and applications connected to the eduID
                            Service.
                        </li>
                        <li><strong>User</strong> <br/>
                            the natural person who is the holder of an eduID Account, as offered and managed by SURF.
                        </li>
                        <li><strong>Terms of use</strong> <br/>
                            these terms of use.
                        </li>
                        <li><strong>SURF</strong> <br/>
                            SURF B.V. SURF makes eduID available to the User.
                        </li>
                    </ul>

                </p>
            </div>
            <div className="card bottom full">
                <h5>
                    2. Rights and obligations of the User
                </h5>
                <p className="info">
                    <strong>2.1</strong> A User has the following standard usage rights to the eduID Service: creating
                    an eduID Account, modifying or enriching personal information, logging in with the eduID Account
                    to applications connected to the eduID Service.
                </p>
                <p className="info">
                    <strong>2.2</strong> The User adheres to these Terms of Use.
                </p>
                <p className="info">
                    <strong>2.3</strong> The User uses his eduID Account in accordance with these terms of use, with
                    specific reference to Article 3 Abuse.
                </p>
                <p className="info">
                    <strong>2.4</strong> The login details for an eduID Account, including username and password or
                    another form of identification/authentication, are strictly personal. The User is not permitted
                    to transfer login details and resulting rights for access to applications connected to the eduID
                    Service to a third party or to allow them to be used.
                </p>
            </div>
            <div className="card bottom full">
                <h5>3. Abuse
                </h5>
                <p className="info">
                    <strong>3.1</strong> Abuse includes – but is not limited to – using the eduID Account in a manner
                    for which the account is not intended. In any case, abuse includes:
                    <ul>
                        <li>Creating an eduID Account with data from a third party;</li>
                        <li>Creating an eduID Account for a legal entity;</li>
                        <li>Phishing or identity fraud;</li>
                        <li>The use of the eduID Account to contravene the law, public order, and public decency.
                        </li>
                    </ul>
                </p>
                <p className="info">
                    <strong>3.2</strong> It is prohibited to use the eduID Account in any of the above-mentioned
                    forms of Abuse or in any other way that is not in accordance with social norms and values.
                </p>
            </div>
            <div className="card bottom full">
                <h5>4. Consequences of Abuse
                </h5>
                <p className="info">
                    <strong>4.1</strong> SURF informs the User in the event of Abuse.
                </p>
                <p className="info">
                    <strong>4.2</strong> If SURF has reason to believe that the User is infringing or is in danger
                    of infringing rules or obligations as described in these Terms of Use, the User is obliged, in
                    consultation with SURF, to end the intended infringement as quickly as possible and for the prevent
                    in the future.
                </p>
                <p className="info">
                    <strong>4.3</strong> SURF reserves the right to (immediately) close a User’s eduID Account without
                    stating a reason and/or to deny the User access to the applications connected to the eduID Service.
                </p>
                <p className="info">
                    <strong>4.4</strong> SURF also has the right to take other measures that are necessary to protect
                    SURF, Users and/or third parties against damage, nuisance, or infringements of rights.
                </p>
            </div>
            <div className="card bottom full">
                <h5>5. Liability
                </h5>
                <p className="info">
                    <strong>5.1</strong> SURF accepts no liability from the User or third parties arising from the
                    User’s failure to comply with the Terms of Use.
                </p>
                <p className="info">
                    <strong>5.2</strong> SURF accepts no liability for any damage suffered by the User or a third
                    party as a result of closing the eduID Account and being denied access to the applications connected
                    to the eduID Service, as referred to in Article 4, paragraph 3 of these Terms of Use.
                </p>
            </div>
            <div className="card bottom full">
                <h5>6. Term
                </h5>
                <p className="info">
                    <strong>6.1</strong> SURF ensures that the eduID Service is available and may, without prior notice,
                    put the eduID Service (temporarily or otherwise) out of operation or limit its use insofar as this
                    is
                    necessary for reasonably necessary maintenance or adjustments and/or improvements to be made by SURF
                    to the Service.
                </p>
                <p className="info">
                    <strong>6.2</strong> SURF is not liable for any damage resulting from the (temporary) unavailability
                    of the Service.
                </p>
            </div>
            <div className="card bottom full">
                <h5>7. Privacy en cookie policy
                </h5>
                <p className="info">
                    <strong>7.1</strong> Read via <Link to={"/privacy"}>this link</Link> how eduID handles your privacy
                    (privacy and cookie policy).
                </p>
            </div>
            <div className="card bottom full">
                <h5>8. Applicable law
                </h5>
                <p className="info">
                    <strong>8.1</strong> Dutch law applies to these conditions and the competent court is the District
                    Court of Central Netherlands, location Utrecht.
                </p>
            </div>
            <div className="card bottom full">
                <h5>9. Changing the Terms of Use
                </h5>
                <p className="info">
                    <strong>9.1</strong> SURF reserves the right to amend these Terms of Use as well as the eduID
                    privacy
                    policy.
                </p>
                <p className="info">
                    <strong>9.2</strong> We therefore advise you to regularly check this page to see if any changes have
                    been made.
                </p>
            </div>
            <div className="card full bottom">
                <h5>10. Support and questions
                </h5>
                <p className="info">
                    <strong>10.1</strong> You can contact <a href="mailto:help@eduid.nl">help@eduid.nl</a>.
                </p>
            </div>
        </Background>
    );
}
