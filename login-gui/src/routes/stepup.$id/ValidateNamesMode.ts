// A const object instead of `enum`: erasableSyntaxOnly forbids enums.
export const ValidateNamesMode = {
    All: 'all',
    InstitutionOnly: 'institutionOnly',
    AlternativesOnly: 'alternativesOnly',
} as const;

export type ValidateNamesMode =
    (typeof ValidateNamesMode)[keyof typeof ValidateNamesMode];

export const LINKED_INSTITUTION = 'linked_institution';
export const VALIDATE_NAMES = 'validate_names';
export const AFFILIATION_STUDENT = 'affiliation_student';

// Unused for now
export const VALIDATE_NAMES_EXTERNAL = 'validate_names_external';
export const PROFILE_MFA = 'profile_mfa';
export const ANY_LINK = 'any_link';

export const SUPPORTED_EXPLANATIONS = [
    VALIDATE_NAMES,
    LINKED_INSTITUTION,
    AFFILIATION_STUDENT,
] as const;

export type SupportedExplanation = (typeof SUPPORTED_EXPLANATIONS)[number];

export const MODE_BY_EXPLANATION: Record<
    SupportedExplanation,
    ValidateNamesMode
> = {
    [VALIDATE_NAMES]: ValidateNamesMode.All,
    [LINKED_INSTITUTION]: ValidateNamesMode.InstitutionOnly,
    [AFFILIATION_STUDENT]: ValidateNamesMode.InstitutionOnly,
};

export const isSupportedExplanation = (
    value: string | null,
): value is SupportedExplanation =>
    SUPPORTED_EXPLANATIONS.some((explanation) => explanation === value);
