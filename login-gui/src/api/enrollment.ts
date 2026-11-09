import { createEnrollmentApi } from '@myconext/shared-gui';
import type { EnrollmentScope } from '@myconext/shared-gui';

import { apiFetch } from '../utils/apiFetch.ts';

export { toSendError } from '@myconext/shared-gui';
export type {
    BackupCodeResponse,
    EnrollmentStatus,
    SendError,
    StartEnrollmentResponse,
    VerifyPhoneCodeResponse,
} from '@myconext/shared-gui';

const enrollment = createEnrollmentApi(apiFetch);

const hashScope = (hash: string): EnrollmentScope => ({ kind: 'hash', hash });

export const startEnrollment = (hash: string) =>
    enrollment.startEnrollment(hashScope(hash));

export const { pollEnrollment } = enrollment;

export const sendPhoneCode = (hash: string, phoneNumber: string) =>
    enrollment.sendPhoneCode(hashScope(hash), phoneNumber);

export const verifyPhoneCode = (hash: string, phoneVerification: string) =>
    enrollment.verifyPhoneCode(hashScope(hash), phoneVerification);

export const generateBackupCode = (hash: string) =>
    enrollment.generateBackupCode(hashScope(hash));
