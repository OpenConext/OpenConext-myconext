import "./App.scss";
import {Navigate, Route, Routes, useLocation} from "react-router";
import {Home} from "./routes/Home.jsx";
import {About} from "./routes/About.jsx";
import {Support} from "./routes/Support.jsx";
import NotFound from "./routes/NotFound.jsx";
import {Header} from "./components/Header.jsx";
import {Footer} from "./components/Footer.jsx";
import {useEffect} from "react";
import {useAppStore} from "./stores/AppStore.js";
import {MobileNavigation} from "./routes/MobileNavigation.jsx";
import {Terms} from "./routes/Terms.jsx";
import {Privacy} from "./routes/Privacy.jsx";
import {InstallApp} from "./routes/InstallApp.jsx";
import {ServiceDesk} from "./routes/ServiceDesk.jsx";

function App() {

    const currentLocation = useLocation();

    useEffect(() => {
        const host = window.location.host;//test.eduid.nl
        useAppStore.setState(() => ({
            config: {
                idpBaseUrl: `https://login.${host}`,
                spBaseUrl: `https://mijn.${host}`
            }
        }))
    }, []);

    return (
        <div className="eduid">
            <div className="container">
                <Header currentLocation={currentLocation}/>
                <Routes>
                    <Route path="/" element={<Navigate replace to="home"/>}/>
                    <Route path="/home" element={<Home/>}/>
                    <Route path="/about" element={<About/>}/>
                    <Route path="/nav" element={<MobileNavigation/>}/>
                    <Route path="/support" element={<Support/>}/>
                    <Route path="/privacy" element={<Privacy/>}/>
                    <Route path="/terms" element={<Terms/>}/>
                    <Route path="/install-app" element={<InstallApp/>}/>
                    <Route path="/servicedesk" element={<ServiceDesk/>}/>
                    <Route path="/servicedesk-en" element={<ServiceDesk/>}/>
                    <Route path="*" element={<NotFound/>}/>
                </Routes>
                <Footer/>
            </div>
        </div>);
}

export default App
