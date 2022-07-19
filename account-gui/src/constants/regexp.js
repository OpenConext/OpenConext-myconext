const mailRegExp = /(.+)@(.+)\.(.+)/;
const verificationCodeRegExp = /^[A-Z0-9]{3}-[A-Z0-9]{3}$/;
const passwordRegExp = /^(((?=.*[A-Z])(?=.*[0-9])(.{8,}))|(.{15,}))$/;
const phoneRegExp = /^\+?(?:[0-9] ?){6,14}[0-9]$/;

export const validEmail = email => mailRegExp.test(email);

export const validVerificationCode = verificationCode => verificationCodeRegExp.test(verificationCode);

export const validPassword = password => passwordRegExp.test(password);

export const validPhoneNumber = phoneNumber => phoneNumber &&
    phoneRegExp.test(phoneNumber.replaceAll(" ","").replaceAll("-",""));