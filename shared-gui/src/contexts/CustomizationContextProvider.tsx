import type { FC } from 'react';

import {
    AppCustomizationContext,
    type CustomizationContextProps,
} from './customizationContext';

export const CustomizationContextProvider: FC<CustomizationContextProps> = ({
    children,
    appCustomization,
}) => {
    return (
        <AppCustomizationContext.Provider value={appCustomization}>
            {children}
        </AppCustomizationContext.Provider>
    );
};
