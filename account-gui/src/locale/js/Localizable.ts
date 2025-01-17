// DO NOT EDIT. This file is auto-generated using Localicious (https://github.com/PicnicSupermarket/localicious).

import en from "./en/strings.json";
import nl from "./nl/strings.json";

type LanguageStrings = | typeof en | typeof nl;

type Language = | "en" | "nl";

type EnumDictionary<T extends string | symbol | number, U> = {
  [K in T]: U;
};

export type TranslationKey = keyof LanguageStrings;

const strings: EnumDictionary<Language, LanguageStrings> = {
  en: en,
  nl: nl,
};

const format = (string: string, ...args: string[]) => {
  let result = string;
  for (let i = 0; i < args.length; i++) {
    const pos = i + 1;
    if (typeof args[i] === "string") {
      result = result.replace("%" + pos + "$s", args[i]);
    }
    if (typeof args[i] === "number") {
      result = result.replace("%" + pos + "$d", args[i]);
    }
  }
  return result;
};

export const translation = (
  language: Language,
  key: TranslationKey,
  ...args: string[]
): string | undefined => {
  // Check if translation exists.
  if (!(language in strings) && !(key in strings[language])) {
    return undefined;
  }
  return format(strings[language][key], ...args);
};
