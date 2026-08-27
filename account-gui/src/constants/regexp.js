const mailRegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phoneRegExp = /^\+?(?:[0-9] ?){6,14}[0-9]$/;

export const validEmail = email => mailRegExp.test(email);

//This only gates the login submit button for an already-set password, so it's a simple non-empty check;
//the actual strength policy (length, second-factor floor) is enforced when a password is set, not here.
export const validPassword = password => (password || "").length > 0;

export const validPhoneNumber = phoneNumber => phoneNumber &&
    phoneRegExp.test(phoneNumber.replaceAll(" ","").replaceAll("-",""));