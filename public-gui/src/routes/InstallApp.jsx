import {useEffect} from "react";
import I18n from "../locale/I18n.js";
import {useNavigate} from "react-router";

export const InstallApp = () => {

    const navigate = useNavigate();

    useEffect(() => {
        const url = detectMobileOS();
        if (url.startsWith("http")) {
            window.location.href = url;
        } else {
            navigate(url);
        }
    }, [navigate]);

    const detectMobileOS = () => {
        const userAgent = navigator.userAgent || navigator.vendor || window.opera;
        if (/iPad|iPhone|iPod/.test(userAgent) && !window.MSStream) {
            return I18n.t("home.apple");
        }
        if (/android/i.test(userAgent)) {
            return I18n.t("home.google");
        }
        return "/home";
    }

}