import type { AppCustomization } from '../contexts/CustomizationContext';
import type { AppTranslationOverride } from '../locale/translationType';
import { appCustomization as defaultAppCustomization } from './defaultCustomApp';

type MyCustomizationsModule = {
    appCustomization?: AppCustomization;
    enOverride?: AppTranslationOverride;
    nlOverride?: AppTranslationOverride;
};

const overrides = import.meta.glob('./myCustomizations/index.ts', {
    eager: true,
}) as Record<string, MyCustomizationsModule>;
const mod: MyCustomizationsModule =
    overrides['./myCustomizations/index.ts'] ?? {};

export const appCustomization: AppCustomization =
    mod.appCustomization ?? defaultAppCustomization;
export const enOverride: AppTranslationOverride | undefined = mod.enOverride;
export const nlOverride: AppTranslationOverride | undefined = mod.nlOverride;
