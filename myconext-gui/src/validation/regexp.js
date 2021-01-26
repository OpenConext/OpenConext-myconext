const mailRegExp = /(.+)@(.+)\.(.+)/;

export const validEmail = email => mailRegExp.test(email);

export const validPassword = password => /^(((?=.*[A-Z])(?=.*[0-9])(.{8,}))|(.{15,}))$/.test(password);
