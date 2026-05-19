import {useEffect} from "react";

export const TiqrAuth = () => {

    useEffect(() => {
        const searchParams = new URLSearchParams(window.location.search);
        const u = searchParams.get("u");
        const s = searchParams.get("s");
        const q = searchParams.get("q");
        const i = searchParams.get("i");
        const v = searchParams.get("v");

        if (u && s && q && i && v) {
            window.location.href = `eduidauth://${u}@${i}/${s}/${q}/${i}/${v}`;
        }
    }, []);


}