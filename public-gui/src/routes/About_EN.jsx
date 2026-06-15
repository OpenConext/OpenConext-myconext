import about from "../assets/about.svg";
import {Background} from "../components/Background.jsx";
import {InfoLinkField} from "../components/InfoLinkField.jsx";
import {Link, useNavigate} from "react-router";
import {useFragmentOpen} from "../hooks/useFragmentOpen.js";
import I18n from "../locale/I18n.js";
import {useAppStore} from "../stores/AppStore.js";
import {Button} from "@surfnet/sds";
import {QRCode} from "react-qr-code";
import appStore from "../assets/app_store.svg";
import googlePlay from "../assets/google_play.svg";

export const About_EN = () => {

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
                        <h2>One account for educational and research institutions</h2>
                    </div>
                </div>
            </div>
            <Background>
                <div className="card">
                    <h2>What is eduID?</h2>
                    <p className="info">
                        eduID is your personal account for logging in to educational and research institutions in
                        the Netherlands, whether you're a student, employee, or professional. Your eduID stays
                        with you, even if you change institutions or jobs.
                    </p>
                </div>
                <div className="card bottom with-collapse-fields">
                    <h2>How do you use eduID?</h2>
                    <InfoLinkField id="1-create-an-eduid" title="1. Create an eduID"
                                   isOpen={openId === "1-create-an-eduid"} onToggle={handleToggle}>
                        <p>Click 'Create eduID' and follow the steps.</p>
                        <p>Use a <strong>personal email address</strong> that you will continue to use, even if
                            you change schools or jobs.</p>
                        <Button onClick={() => window.location.href = `${config.idpBaseUrl}/register`}
                                txt={I18n.t("header.register")}/>

                    </InfoLinkField>
                    <InfoLinkField id="2-verify-your-identity-with-an-official-party"
                                   title="2. Verify your identity with an official party"
                                   isOpen={openId === "2-verify-your-identity-with-an-official-party"}
                                   onToggle={handleToggle}>
                        <p>In most cases, you'll need to verify your identity before you can use your eduID. You
                            can do this in one of the following ways:</p>
                        <p className="section-label top"><strong>With your Dutch educational or research
                            institution</strong></p>
                        <p>By linking your institution, you add the following to your eduID in one go:</p>
                        <ul className="verify-options">
                            <li>Your full name, confirmed by an official party</li>
                            <li>Proof that you are a student or employee</li>
                            <li>Your current institution</li>
                        </ul>
                        <p className="section-label"><strong>Here's how:</strong></p>
                        <p>Open the eduID app or go to My eduID and select 'Link your institution'. Or click
                            'verify your identity' and then 'use your school or work account'.</p>
                        <p className="separator">You log in to your institution, after which it automatically shares
                            your name and role
                            with us.</p>
                        <p className="quote">Please note: your institution data will be automatically deleted after 6
                            months. After
                            that, you can easily re-link your institution.</p>
                        <p className="section-label top"><strong>Not affiliated with an institution?</strong> You can
                            verify your eduID in the
                            following ways:</p>
                        <ul>
                            <li><strong>Via your bank (iDIN)</strong> – for customers of a Dutch bank</li>
                            <li><strong>European identity document (eIDAS)</strong> – for citizens from other
                                European countries
                            </li>
                            <li><strong><Link to={"/servicedesk"}>Service desk</Link></strong> – only for participants
                                in eduBadges pilots
                            </li>
                        </ul>
                        <Button onClick={() => navigate("/verify")} txt={"Learn more about verifying your eduID"}/>
                    </InfoLinkField>
                    <InfoLinkField id="3-log-in-to-websites-and-other-online-services"
                                   title="3. Log in to websites and other online services"
                                   isOpen={openId === "3-log-in-to-websites-and-other-online-services"}
                                   onToggle={handleToggle}>
                        <p>With your eduID you can log in to websites and other online services in the
                            Netherlands. We recommend using the eduID app, as it is the most secure and convenient
                            option. The app lets you log in without a password, and two-factor authentication
                            (2FA) provides an extra layer of security.</p>
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
                    <h2>Where can you find your account and details?</h2>
                    <p className="info">
                        In <a href="https://mijn.eduid.nl">My eduID</a> you'll find all your details, such as your name,
                        email address, and security
                        settings. You can also see which services you've logged in to with eduID, and update your
                        information whenever you like.
                    </p>
                </div>
            </Background>
        </div>
    );
}
