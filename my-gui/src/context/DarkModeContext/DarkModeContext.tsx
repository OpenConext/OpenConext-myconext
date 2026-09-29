import { createContext, useContext, useEffect, useState } from 'react';

interface DarkModeContextValue {
    dark: boolean;
    setDark: React.Dispatch<React.SetStateAction<boolean>>;
}

const DarkModeContext = createContext<DarkModeContextValue | undefined>(
    undefined,
);

export function DarkModeProvider({ children }: { children: React.ReactNode }) {
    const [dark, setDark] = useState(false);

    useEffect(() => {
        document.documentElement.classList.toggle('dark', dark);
    }, [dark]);

    return (
        <DarkModeContext.Provider value={{ dark, setDark }}>
            {children}
        </DarkModeContext.Provider>
    );
}

export function useDarkMode() {
    const context = useContext(DarkModeContext);
    if (!context) {
        throw new Error('useDarkMode must be used within a DarkModeProvider');
    }
    return context;
}
