import { createContext, useContext } from 'react';
import type { ReactNode } from 'react';

// A thenable is a ReactNode, but returning one from this client render throws.
export type CustomizationNode = Exclude<ReactNode, Promise<unknown>>;

export type AppCustomization = {
    appLogo: CustomizationNode;
};

export const AppCustomizationContext = createContext<AppCustomization>({
    appLogo: undefined,
});

export type CustomizationContextProps = {
    appCustomization: AppCustomization;
    children: ReactNode;
};

export const useCustomization = () => useContext(AppCustomizationContext);

// Keeps the override key limited to slots that render as React nodes.
type KeysOfType<T, V> = {
    [K in keyof T]: T[K] extends V ? K : never;
}[keyof T];

export type OverridableComponentProps = {
    appCustomizationReactNodeKey: KeysOfType<AppCustomization, ReactNode>;
    children: ReactNode;
};
