import "./Verification.scss";
import React, {useState} from "react";
import I18n from "../locale/I18n";
import {Button, CodeValidation, ErrorIndicator} from "@surfnet/sds";
import alertIcon from "../icons/alert.svg";
import {getUserControlCode} from "../api/index.js";
import {useAppStore} from "../stores/AppStore.js";

const Verification = ({proceed}) => {

    const [clear, setClear] = useState(false);
    const [error, setError] = useState(false);
    const [code, setCode] = useState("");

    const doGetUserControlCode = val => {
        setCode(val);
        getUserControlCode(val)
            .then(controlCode => {
                useAppStore.setState(() => ({controlCode: controlCode}));
                setClear(false);
                setCode("");
                setError(false);
                proceed();
            })
            .catch(() => {
                setClear(true);
                setTimeout(() => setClear(false), 20);
                setError(true);
            });
    }

    return (
        <div className="verification">
            <h4>{I18n.t("verification.header")}</h4>
            <div className="code-validation">
                <CodeValidation verify={val => doGetUserControlCode(val)}
                                size={5}
                                clear={clear}
                                intermediateCallback={() => setError(false)}
                                validate={val => !isNaN(val)}
                />
                <Button txt={I18n.t("verification.proceed")}
                        disabled={true}
                        onClick={() => true}
                />
            </div>
            {error && <ErrorIndicator message={I18n.t("verification.error", {code: code})}/>}
            <div className="disclaimer">
                <img src={alertIcon} alt=""/>
                <p>{I18n.t("verification.info")}</p>
            </div>

        </div>
    );

};

export default Verification;