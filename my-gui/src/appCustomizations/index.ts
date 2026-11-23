import type { AppCustomization } from '@myconext/shared-gui';

import { appCustomization as defaultAppCustomization } from './defaultCustomApp';

type MyCustomizationsModule = {
    appCustomization?: AppCustomization;
};

const overrides = import.meta.glob('./myCustomizations/index.ts', {
    eager: true,
}) as Record<string, MyCustomizationsModule>;
const mod: MyCustomizationsModule =
    overrides['./myCustomizations/index.ts'] ?? {};

export const appCustomization: AppCustomization =
    mod.appCustomization ?? defaultAppCustomization;
