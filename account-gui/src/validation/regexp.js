const mailRegExp = /(.+)@(.+)\.(.+)/;

export const validEmail = email => mailRegExp.test(email);

