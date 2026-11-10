// the server shows the recovery code in two parts
export const formatRecoveryCode = (code: string): string =>
    `${code.substring(0, 4)} ${code.substring(4)}`;
