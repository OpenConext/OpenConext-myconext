import {useEffect, useState} from "react";

export const useFragmentOpen = () => {
    const [openId, setOpenId] = useState(null);

    useEffect(() => {
        const hash = window.location.hash.slice(1);
        if (hash) {
            setOpenId(hash);
            setTimeout(() => {
                const el = document.getElementById(hash);
                if (el) el.scrollIntoView({behavior: "smooth"});
            }, 50);
        } else {
            window.scroll(0, 0);
        }
    }, []);

    const handleToggle = (id) => {
        setOpenId(prev => prev === id ? null : id);
    };

    return {openId, handleToggle};
};
