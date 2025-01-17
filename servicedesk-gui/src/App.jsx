import {useEffect, useState} from 'react'
import {Loader} from "@surfnet/sds";
import './App.scss'
// import {useNavigate} from "react-router-dom";
// import {useAppStore} from "./stores/AppStore.js";
import {configuration, me} from "./api/index.js";
import {useAppStore} from "./stores/AppStore.js";

const App = () => {
    const [loading, setLoading] = useState(true);

    // async function fetchConfig() {
    //     try {
    //         setConf(await configuration());
    //         setLoading(false)
    //     } catch (e) {
    //         console.log(e);
    //     }
    // }
    useEffect(() => {
        // fetchConfig();
        //  configurationAsync().then(res => {
        //     setConf(res);

        //     setLoading(false);
        // })
        configuration().then(res => {
            useAppStore.setState(() => ({config: res}));
            me().then(json => {
                useAppStore.setState(() => ({user: json, authenticated: true}));
                setLoading(false);
                // const {user, config} = useAppStore(state => state);
            });

        });

    }, []);// eslint-disable-line react-hooks/exhaustive-deps

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