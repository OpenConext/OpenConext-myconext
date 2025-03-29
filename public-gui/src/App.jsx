import "./App.scss";
import {Navigate, Route, Routes, useLocation} from "react-router";
import {Home} from "./routes/Home.jsx";
import {About} from "./routes/About.jsx";
import {Support} from "./routes/Support.jsx";
import NotFound from "./routes/NotFound.jsx";
import {Header} from "./components/Header.jsx";
import {Footer} from "./components/Footer.jsx";
import {useEffect} from "react";
import {configuration} from "./api/index.js";
import {useAppStore} from "./stores/AppStore.js";
import {MobileNavigation} from "./routes/MobileNavigation.jsx";

function App() {

    const currentLocation = useLocation();

    // useEffect(() => {
    //     configuration().then(res => {
    //         useAppStore.setState(() => ({config: res}))
    //     });
    // }, []);

    return (
        <div className="eduid">
            <div className="container">
                <Header currentLocation={currentLocation}/>
                <Routes>
                    <Route path="/" element={<Navigate replace to="home"/>}/>
                    <Route path="home" element={<Home/>}/>
                    <Route path="about" element={<About/>}/>
                    <Route path="nav" element={<MobileNavigation/>}/>
                    <Route path="support" element={<Support/>}/>
                    <Route path="*" element={<NotFound/>}/>
                </Routes>
                <Footer/>
            </div>
        </div>);
}

export default App
