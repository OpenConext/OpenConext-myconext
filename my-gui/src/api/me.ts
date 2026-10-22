import type { Activity } from './activities';
import type { ControlCode } from './identity';
import { request } from './request';

export type LinkedAccount = {
    // institution accounts
    displayNameEn?: string;
    displayNameNl?: string;
    schacHomeOrganization?: string;
    logoUrl?: string;
    eduPersonPrincipalName?: string;
    subjectId?: string;
    eduPersonAffiliations?: string[];
    // iDIN and eIDAS accounts
    issuer?: { id: string; name: string };
    givenName?: string;
    familyName?: string;
    // iDIN and eIDAS accounts use other name fields
    firstName?: string;
    legalLastName?: string;
    idpScoping?: string;
    // false when the connection to Studielink has ended
    connectionActive?: boolean;
    // epoch millis, only delivered by iDIN and eIDAS accounts
    dateOfBirth?: number | null;
    preferred: boolean;
    createdAt: number;
    expiresAt: number;
    external: boolean;
};

export type PublicKeyCredential = {
    identifier: string;
    name: string;
    createdAt: number;
};

export type AppRegistration = {
    // APNS and FCM, with or without _DIRECT
    notificationType?: string;
    created?: number;
    updated?: number;
    // true when the user saved a recovery code instead of a phone number
    recoveryCode?: boolean;
    // the last digits of the recovery phone number
    phoneNumber?: string;
};

export type ServiceInfo = {
    entityId: string;
    name: string;
    nameNl?: string;
    logoUrl?: string;
    homeUrl?: string;
    createdAt: number;
    lastLogin?: number;
};

export type EduIdPerServiceProvider = {
    // the unique eduID for this service provider
    value: string;
    services: ServiceInfo[];
    createdAt: number;
    expiresIn?: number;
};

export type User = {
    id: string;
    email: string;
    chosenName: string;
    givenName: string;
    familyName: string;
    // epoch seconds
    created?: number;
    preferredLanguage?: string;
    loginOptions?: string[];
    usePassword?: boolean;
    // epoch millis
    passwordUpdatedAt?: number;
    publicKeyCredentials?: PublicKeyCredential[];
    registration?: AppRegistration;
    linkedAccounts?: LinkedAccount[];
    externalLinkedAccounts?: LinkedAccount[];
    // epoch millis, derived from the verified accounts
    dateOfBirth?: number | null;
    // the most recent activities, all activities are available in fetchActivities
    recentActivities?: Activity[];
    controlCode?: ControlCode | null;
    // keyed by the entity id of the service provider
    eduIdPerServiceProvider?: Record<string, EduIdPerServiceProvider>;
    [key: string]: unknown;
};

export const fetchMe = () => request<User>('/myconext/api/sp/me');
