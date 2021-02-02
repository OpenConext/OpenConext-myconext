const mailRegExp = /(.+)@(.+)\.(.+)/;
const verificationCodeRegExp = /[A-Z0-9]{3}-[A-Z0-9]{3}/;

export const validEmail = email => mailRegExp.test(email);

export const validVerificationCode = verificationCode => verificationCodeRegExp.test(verificationCode);
