const mailRegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const passwordRegExp = /^(((?=.*[A-Z])(?=.*[0-9])(.{8,}))|(.{15,}))$/;
const phoneRegExp = /^\+?(?:[0-9] ?){6,14}[0-9]$/;

export const validEmail = email => mailRegExp.test(email);

export const validPassword = password => passwordRegExp.test(password);

export const validPhoneNumber = phoneNumber => phoneNumber &&
    phoneRegExp.test(phoneNumber.replaceAll(" ","").replaceAll("-",""));