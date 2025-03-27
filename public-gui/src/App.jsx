import './App.scss'
import {Navigate, Route, Routes} from "react-router";
import {Home} from "./routes/Home.js";
import {About} from "./routes/About.js";
import {Support} from "./routes/Support.js";
import NotFound from "./routes/NotFound.js";
import {Header} from "./components/Header.jsx";

function App() {

    return (
        <div className="eduid">
            <div className="container">
                <Header/>
                <Routes>
                    <Route path="/" element={<Navigate replace to="home"/>}/>
                    <Route path="home" element={<Home/>}/>
                    <Route path="about" element={<About/>}/>
                    <Route path="support" element={<Support/>}/>
                    <Route path="*" element={<NotFound/>}/>
                </Routes>
                <Footer/>
            </div>
        </div>);
}

export default App
