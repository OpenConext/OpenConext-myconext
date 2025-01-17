import {useEffect, useState} from 'react'
import {Loader} from "@surfnet/sds";
import './App.scss'
// import {useNavigate} from "react-router-dom";
// import {useAppStore} from "./stores/AppStore.js";
import {configuration} from "./api/index.js";
import {useAppStore} from "./stores/AppStore.js";

const App = () => {

    const [loading, setLoading] = useState(true);

    useEffect(() => {
        configuration().then(res => {
            useAppStore.setState(() => ({config: res, authenticated: res.authenticated, user: res.user}));
            setLoading(false);
        });

    }, []);

    if (loading) {
        return <Loader/>
    }

    return (
        <>
            <div>
                <p>Boem</p>
            </div>
        </>
    );
}

export default App;