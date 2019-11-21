export const validPassword = password => /^(((?=.*[A-Z])(?=.*[0-9])[a-zA-Z\d]{8,})|(.{15,}))$/.test(password);
