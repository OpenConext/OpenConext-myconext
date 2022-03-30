const mailRegExp = /(.+)@(.+)\.(.+)/;
const verificationCodeRegExp = /^[A-Z0-9]{3}-[A-Z0-9]{3}$/;
const passwordRegExp = /^(((?=.*[A-Z])(?=.*[0-9])(.{8,}))|(.{15,}))$/;

export const validEmail = email => mailRegExp.test(email);

export const validVerificationCode = verificationCode => verificationCodeRegExp.test(verificationCode);

export const validPassword = password => passwordRegExp.test(password);