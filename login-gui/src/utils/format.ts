// dates of birth are stored as midnight UTC
export const formatDateOfBirth = (date: number | string): string => {
    const d = new Date(date);
    const pad = (n: number) => `${n}`.padStart(2, '0');
    return `${pad(d.getUTCDate())}-${pad(d.getUTCMonth() + 1)}-${d.getUTCFullYear()}`;
};
