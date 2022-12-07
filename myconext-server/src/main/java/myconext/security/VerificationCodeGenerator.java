package myconext.security;

import java.security.SecureRandom;
import java.util.Random;

public class VerificationCodeGenerator {

    private static final char[] DEFAULT_CODEC = "346789ABCDEFGHJKMNPQRTUVWXY"
            .toCharArray();

    private static final char[] NUMBERS = "1234567890"
            .toCharArray();

    private static final Random random = new SecureRandom();

    private VerificationCodeGenerator() {
    }

    public static String generate() {
        char[] chars = getChars(6, DEFAULT_CODEC);
        return String.valueOf(chars, 0, 3) + "-" + String.valueOf(chars, 3, 3);
    }

    public static String generateBackupCode() {
        char[] chars = getChars(8, NUMBERS);
        return String.valueOf(chars, 0, 4) + " " + String.valueOf(chars, 4, 4);

    }

    public static String generatePhoneVerification() {
        return String.valueOf(getChars(6, NUMBERS));

    }

    private static char[] getChars(int nbrBytes, char[] charArray) {
        byte[] verifierBytes = new byte[nbrBytes];
        random.nextBytes(verifierBytes);
        char[] chars = new char[verifierBytes.length];
        for (int i = 0; i < verifierBytes.length; i++) {
            chars[i] = charArray[random.nextInt(charArray.length)];
        }
        return chars;
    }


}
