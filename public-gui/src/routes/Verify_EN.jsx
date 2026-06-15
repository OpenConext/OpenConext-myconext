import verify from "../assets/verify.svg";
import {Background} from "../components/Background.jsx";
import {InfoLinkField} from "../components/InfoLinkField.jsx";
import {Link} from "react-router";
import {useFragmentOpen} from "../hooks/useFragmentOpen.js";

export const Verify_EN = () => {

    const {openId, handleToggle} = useFragmentOpen();

    return (
        <div className="verify-container">
            <div className="verify">
                <div className="top">
                    <img src={verify} className="verify" alt=""/>
                    <div className="top-right">
                        <h1 className="title small">eduID</h1>
                        <h2>Verify your identity</h2>
                    </div>
                </div>
            </div>
            <Background>
                <div className="card">
                    <p className="info">
                        With a verified eduID you can prove your identity at educational and research institutions.
                        There are four ways to verify yourself. Choose the option that works best for you.
                    </p>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>Verification methods</h2>
                    <InfoLinkField id="through-your-institution" title="Through your institution"
                                   isOpen={openId === "through-your-institution"} onToggle={handleToggle}>
                        <p>Are you a student or employee? Link your eduID to your institution's account.</p>
                        <ol>
                            <li>Go to <a href="https://mijn.eduid.nl">My eduID</a> and log in.</li>
                            <li>Go to <strong>Your details</strong> and click <strong>Verify your identity</strong> or <strong>Add an organisation</strong>.</li>
                            <li>Click <strong>Use your school or work account</strong> and follow the steps.</li>
                            <li>Your eduID is verified immediately.</li>
                        </ol>
                        <p className="quote">You only need to do this once. Have you already verified your identity before? Then you
                            don't need to follow these steps.</p>
                        <p><strong>Not affiliated with an institution?</strong> Use one of the methods below to verify your
                            eduID.</p>
                    </InfoLinkField>
                    <InfoLinkField id="through-your-bank-idin" title="Through your bank (iDIN)"
                                   isOpen={openId === "through-your-bank-idin"} onToggle={handleToggle}>
                        <p>iDIN is a system that allows you to identify yourself online through your bank.</p>
                        <ol>
                            <li>Go to <a href="https://mijn.eduid.nl">My eduID</a> and log in.</li>
                            <li>Go to <strong>Your details</strong> and click <strong>Verify your identity</strong>.</li>
                            <li>Click <strong>Use your bank</strong>.</li>
                            <li>Choose your bank, log in and confirm. You only share your personal details, not any
                                financial information.
                            </li>
                            <li>Your eduID is verified immediately.</li>
                        </ol>
                        <p className="quote">Almost all Dutch banks participate in iDIN. Check at idin.nl whether your bank is
                            connected.</p>
                    </InfoLinkField>
                    <InfoLinkField id="through-your-european-identity-document-eidas"
                                   title="Through your European identity document (eIDAS)"
                                   isOpen={openId === "through-your-european-identity-document-eidas"} onToggle={handleToggle}>
                        <p>eIDAS is a European law that ensures secure and reliable digital identification across
                            Europe. Are you a European citizen (non-Dutch)? Then you can verify your eduID account
                            using your country's digital identity tool, similar to DigiD.</p>
                        <ol>
                            <li>Go to <a href="https://mijn.eduid.nl">My eduID</a> and log in.</li>
                            <li>Go to <strong>Your details</strong> and click <strong>Verify your identity</strong>.</li>
                            <li>Click <strong>Use a European ID</strong>.</li>
                            <li>Select your country and follow the steps.</li>
                            <li>Your eduID is verified immediately.</li>
                        </ol>
                        <p className="quote">Not all European countries are connected. View the current list on the government
                            website. Are you Dutch? Use iDIN or your institution account instead.</p>
                    </InfoLinkField>
                    <InfoLinkField id="at-an-eduid-service-desk" title="At an eduID service desk"
                                   isOpen={openId === "at-an-eduid-service-desk"} onToggle={handleToggle}>
                        <p>Unable to use any of the other options? Verify your identity in person at a <Link
                            to="/servicedesk">service desk</Link>. Please note: the service desk is currently only
                            available for participants in eduBadges pilots.</p>
                        <ol>
                            <li>Go to <a href="https://mijn.eduid.nl">My eduID</a> and log in.</li>
                            <li>Go to <strong>Your details</strong> and click <strong>Verify your identity</strong>.</li>
                            <li>Click <strong>Contact the service desk</strong> and follow the steps to generate a verification
                                code.
                            </li>
                            <li>Bring the verification code and a valid ID to a service desk at your
                                institution.
                            </li>
                            <li>Have your eduID verified.</li>
                        </ol>
                        <p>Service desks are currently available at:</p>
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
                                <td>TU Delft students only</td>
                                <td>Awarding microcredentials</td>
                                <td><a href="mailto:learningforlife@tudelft.nl">learningforlife@tudelft.nl</a>
                                </td>
                            </tr>
                            <tr>
                                <td>Radboud Universiteit</td>
                                <td>RU students only</td>
                                <td>Awarding microcredentials</td>
                                <td><a
                                    href="mailto:onderwijsvoorprofessionals@ru.nl">onderwijsvoorprofessionals@ru.nl</a>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <p className="quote">Is your institution not listed? First check whether you can use another verification
                            method. If not, please contact your institution.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>Frequently asked questions</h2>
                    <InfoLinkField id="my-bank-is-not-listed-under-idin" title="My bank is not listed under iDIN."
                                   isOpen={openId === "my-bank-is-not-listed-under-idin"} onToggle={handleToggle}>
                        <p>Choose a different verification method, such as through your institution. You can find
                            which banks are connected at idin.nl.</p>
                    </InfoLinkField>
                    <InfoLinkField id="i-dont-have-a-valid-id-for-the-service-desk"
                                   title="I don't have a valid ID for the service desk."
                                   isOpen={openId === "i-dont-have-a-valid-id-for-the-service-desk"} onToggle={handleToggle}>
                        <p>Verification via the service desk is not possible without a valid ID. First apply for a
                            new document, or use another method such as your institution account or bank. A student
                            card is not a valid ID.</p>
                    </InfoLinkField>
                    <InfoLinkField id="my-institution-does-not-have-an-eduid-service-desk"
                                   title="My institution does not have an eduID service desk"
                                   isOpen={openId === "my-institution-does-not-have-an-eduid-service-desk"} onToggle={handleToggle}>
                        <p>Unfortunately, you cannot use this verification method in that case. Please contact
                            your institution.</p>
                    </InfoLinkField>
                    <InfoLinkField id="can-i-change-my-name-after-verification" title="Can I change my name after verification?"
                                   isOpen={openId === "can-i-change-my-name-after-verification"} onToggle={handleToggle}>
                        <p>You can always change your display name yourself in <a href="https://mijn.eduid.nl">My eduID</a>. To update your verified
                            name, contact the party you verified with - your bank, institution, or your country's
                            authority.</p>
                    </InfoLinkField>
                    <InfoLinkField id="i-am-receiving-an-error-message-what-can-i-do"
                                   title="I am receiving an error message. What can I do?"
                                   isOpen={openId === "i-am-receiving-an-error-message-what-can-i-do"} onToggle={handleToggle}>
                        <ul>
                            <li>Try again later. Your bank or European identity service may be temporarily
                                unavailable.
                            </li>
                            <li>Have you created multiple eduIDs? It is not possible to verify multiple accounts
                                in the same way.
                            </li>
                            <li>Can't figure it out? Send an email to <a
                                href="mailto:help@eduid.nl">help@eduid.nl</a>.
                            </li>
                        </ul>
                    </InfoLinkField>
                    <InfoLinkField id="i-am-dutch-can-i-verify-with-a-european-identity-document"
                                   title="I am Dutch. Can I verify with a European identity document?"
                                   isOpen={openId === "i-am-dutch-can-i-verify-with-a-european-identity-document"} onToggle={handleToggle}>
                        <p>No, the eIDAS option is intended for citizens from other European countries. Dutch
                            citizens verify via their bank (iDIN) or through their educational or research
                            institution.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom">
                    <h2>I have a different question</h2>
                    <p className="info">Is your question not listed here? Send an email to <a
                        href="mailto:help@eduid.nl">help@eduid.nl</a>. We are happy to help and explain how it works.</p>
                </div>
            </Background>
        </div>
    );
}
