import parachute from "../assets/parachute.svg";
import {Background} from "../components/Background.jsx";
import {InfoLinkField} from "../components/InfoLinkField.jsx";
import {useEffect} from "react";

export const Support_EN = () => {

    useEffect(() => {
        window.scroll(0, 0);
    }, []);

    return (
        <div className="support-container">
            <div className="support">
                <div className="top">
                    <img src={parachute} className="parachute" alt=""/>
                    <div className="top-right">
                        <h2>When you need assistance</h2>
                    </div>
                </div>
            </div>
            <Background>
                <div className="card">
                    <h2>Frequently asked questions</h2>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h3>Account management</h3>
                    <InfoLinkField title="How do I manage my eduID?">
                        <p>Want to view or update your details? Go to <a href="https://mijn.eduid.nl">My eduID</a>. This is where you manage
                            everything related to your eduID.</p>
                    </InfoLinkField>
                    <InfoLinkField title="How do I change my email address?">
                        <p>You can update your email address in <a href="https://mijn.eduid.nl/">My eduID</a>:</p>
                        <ol>
                            <li>Log in at <a href="https://eduid.nl/">eduid.nl</a>.</li>
                            <li>Go to 'Personal info'.</li>
                            <li>Click the 'edit' icon next to your email address.</li>
                        </ol>
                    </InfoLinkField>
                    <InfoLinkField title="How do I delete my eduID?">
                        <p>Want to delete your eduID? Log in at <a href="https://mijn.eduid.nl">My eduID</a> and go to 'Account'. From there you
                            can delete your eduID.</p>
                        <p>Please note: think carefully before deleting your eduID. You may lose access to
                            applications you currently log in to with it, and that access cannot be recovered.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>Personal details</h2>
                    <InfoLinkField title="Do I need multiple eduIDs?">
                        <p>No. You only need one eduID.</p>
                        <ul>
                            <li>You create it with your personal email address.</li>
                            <li>You then add organisations, such as a vocational college, university of applied
                                sciences, or university.
                            </li>
                            <li>Switching studies or jobs? Your eduID stays with you.</li>
                        </ul>
                        <p>Did you accidentally create an eduID with your school or work email address? You can
                            update this in My eduID under 'Personal info'.</p>
                    </InfoLinkField>
                    <InfoLinkField title="My name is incorrect. What can I do?">
                        <p>You cannot change your verified name yourself. However, you can always update your
                            preferred first name.</p>
                        <p>To change your first or last name, you need to do this with the party you used to
                            verify your eduID.</p>
                        <ul>
                            <li><strong>Institution:</strong> contact your school or university.</li>
                            <li><strong>Dutch bank:</strong> update your name with your bank.</li>
                            <li><strong>European ID:</strong> visit the website of the ID provider you used.
                            </li>
                        </ul>
                        <p>Once the change has
                            been made, you can update your name in eduID by first removing the existing
                            details (see below) and then completing the verification again.</p>
                        <p className="separator">Has your name changed due to marriage or a gender change? This is usually arranged
                            through your local municipality first. After that, you can update your name in eduID
                            as well.</p>
                        <p className="separator">Seeing only an initial? That may be correct. When you verify your name via your bank,
                            we only receive the initial(s) of your name. In that case, you can enter your full
                            first name under 'preferred first name'.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Can I delete my verified details?">
                        <p>Yes, you can, but think it through carefully. If you delete your verified details, you
                            may no longer be able to log in to services that currently rely on your eduID.</p>
                        <p>To delete verified details:</p>
                        <ol>
                            <li>Go to Personal info in your eduID.</li>
                            <li>Click the arrow (v) next to the details you want to remove.</li>
                            <li>Click Manage your verified information.</li>
                            <li>Find the details and click Delete this information.</li>
                        </ol>
                    </InfoLinkField>
                    <InfoLinkField title="Why do I need to re-link my institution?">
                        <p>Your institution data is automatically removed after 6 months to keep your details up
                            to date, for example if you switch institutions or roles. You will usually notice this
                            when you are prompted to re-link when trying to log in somewhere.</p>
                        <p>Re-link your institution via the eduID app or <a href="https://mijn.eduid.nl">My eduID</a>.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Can I link multiple organisations to my eduID?">
                        <p>Yes, you can link multiple education or research institutions to your eduID. This may
                            be necessary if you study or work at more than one institution. For each institution,
                            you can verify who you are and in what role (such as student or staff member). Go to
                            <a href="https://mijn.eduid.nl">My eduID</a> and select 'Add institution'.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>Logging in</h2>
                    <InfoLinkField title="My session has expired. What can I do?">
                        <p>You see this message when something goes wrong during login, for example if you wait
                            too long after clicking the login button. Simply log in again.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Can I choose a different login method?">
                        <p>Yes, you can. By default, you receive a login code by email. To use a different method,
                            go to 'Security' in My eduID. There you can see your current login method and change
                            it or add another one, such as a password or a passkey.</p>
                    </InfoLinkField>
                    <InfoLinkField title="What is a passkey and how do I use it?">
                        <p>A passkey is a way to log in without a password. Instead of a password, you use
                            something already on your device, such as your fingerprint, face recognition, or a
                            physical key (like a YubiKey).</p>
                        <p className="section-label"><strong>Adding a passkey</strong></p>
                        <ol>
                            <li>Go to My eduID and log in as you normally would.</li>
                            <li>Select Security on the left and click Add passkey.</li>
                            <li>Give the key a recognisable name (e.g. iPhone fingerprint or Yellow YubiKey) and
                                click Start.
                            </li>
                            <li>Follow your browser's instructions. Done!</li>
                        </ol>
                        <p className="section-label"><strong>Logging in with a passkey</strong></p>
                        <ol>
                            <li>Go to the service you want to log in to with eduID.</li>
                            <li>On the login screen, choose Login with a passkey.</li>
                            <li>Enter your email address and follow your browser's instructions. Done!</li>
                        </ol>
                        <p>Please note: a passkey set up in one browser (e.g. Chrome) does not automatically work
                            in another browser (e.g. Safari). Add the passkey again in that browser.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>eduID app</h2>
                    <InfoLinkField title="I am not receiving the SMS code.">
                        <ul>
                            <li>Have you requested an SMS code multiple times today? You may have reached the
                                limit. Try again after 24 hours.
                            </li>
                            <li>Always use the most recently received SMS code.</li>
                            <li>Still not working? Contact us at <a
                                href="mailto:help@eduid.nl">help@eduid.nl</a>.
                            </li>
                        </ul>
                    </InfoLinkField>
                    <InfoLinkField title="I can no longer access the app or have forgotten my PIN.">
                        <ol>
                            <li>Go to eduid.nl and log in. At the bottom, choose a different login method.</li>
                            <li>Select "Receive a login code in your inbox".</li>
                            <li>You will receive a code by email.</li>
                            <li>After logging in, you can deactivate the app from your account yourself. Go to
                                Security &rarr; "Deactivate your eduID mobile app". Before you can deactivate the
                                app, you will need to enter a recovery code. You will receive this either by SMS,
                                or you will have saved it yourself at some point. This recovery code is different
                                from your login code.
                            </li>
                        </ol>
                        <p>After that, you can add the app again.</p>
                    </InfoLinkField>
                    <InfoLinkField title="I am getting the error message 'registration failed'.">
                        <p>Follow the steps below to resolve this. Make sure you complete step 1 fully before
                            moving on to step 2.</p>
                        <p className="section-label"><strong>Step 1: Remove the app and reset your browser</strong></p>
                        <ul>
                            <li>Delete the eduID app from your phone.</li>
                            <li>Log in to eduid.nl and remove the app from your account.</li>
                            <li>Clear the cookies and (recent) browsing history in both your phone's browser and
                                your computer's browser.
                            </li>
                        </ul>
                        <p className="section-label"><strong>Step 2: Fresh installation</strong></p>
                        <ul>
                            <li>Download the eduID app on your (new) phone.</li>
                            <li>Open a new private/incognito window in your browser.</li>
                            <li>Log in to eduid.nl again.</li>
                            <li>Go to 'Security' and select 'Start app registration'.</li>
                            <li>Follow the on-screen instructions to complete the app registration.</li>
                        </ul>
                    </InfoLinkField>
                    <InfoLinkField title="I cannot scan the QR code.">
                        <p>Check whether the eduID app has permission to use the camera. You can set this in your
                            phone's app settings.</p>
                    </InfoLinkField>
                    <InfoLinkField title="Which phones are supported?">
                        <p>The eduID app works on:</p>
                        <ul>
                            <li>iOS (version 13 and above)</li>
                            <li>Android (official Android versions)</li>
                        </ul>
                        <p>Other operating systems are not supported.</p>
                    </InfoLinkField>
                </div>
                <div className="card bottom">
                    <h2>Need more help?</h2>
                    <p className="info">Can't find your question here? Send an email to <a
                        href="mailto:help@eduid.nl">help@eduid.nl</a>. We're happy to help and explain how it works.</p>
                </div>
            </Background>
        </div>
    );
}
