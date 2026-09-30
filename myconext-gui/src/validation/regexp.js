const mailRegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phoneRegExp = /^\+?(?:[0-9] ?){6,14}[0-9]$/;

//OWASP ASVS V6.2.1: no password shorter than 8 characters
export const MIN_PASSWORD_LENGTH = 8;
//OWASP ASVS V6.2.9: at least 64 characters must be permitted
export const MAX_PASSWORD_LENGTH = 128;

//Counts Unicode code points rather than UTF-16 code units, so surrogate-pair characters count as one
const characterLength = password => Array.from(password || "").length;

export const validEmail = email => mailRegExp.test(email);

export const passwordTooLong = password => characterLength(password) > MAX_PASSWORD_LENGTH;

export const validPassword = password => {
    const length = characterLength(password);
    return length >= MIN_PASSWORD_LENGTH && length <= MAX_PASSWORD_LENGTH;
};

export const validPhoneNumber = phoneNumber => phoneNumber &&
    phoneRegExp.test(phoneNumber.replaceAll(" ","").replaceAll("-",""));
