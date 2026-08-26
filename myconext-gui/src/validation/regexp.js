const mailRegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const passwordRegExp = /^(((?=.*[A-Z])(?=.*[0-9])(.{8,}))|(.{15,}))$/;
const phoneRegExp = /^\+?(?:[0-9] ?){6,14}[0-9]$/;

//BCrypt refuses to hash passwords longer than 72 bytes
export const MAX_PASSWORD_BYTES = 72;

export const validEmail = email => mailRegExp.test(email);

export const passwordTooLong = password => new TextEncoder().encode(password || "").length > MAX_PASSWORD_BYTES;

export const validPassword = password => passwordRegExp.test(password) && !passwordTooLong(password);

export const validPhoneNumber = phoneNumber => phoneNumber &&
    phoneRegExp.test(phoneNumber.replaceAll(" ","").replaceAll("-",""));
