package myconext.security;

import java.security.SecureRandom;
import java.util.Random;

public class VerificationCodeGenerator {

    private static final char[] DEFAULT_CODEC = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZ"
            .toCharArray();

    private static final char[] NUMBERS = "1234567890"
            .toCharArray();

    private static final Random random = new SecureRandom();

    private VerificationCodeGenerator() {
    }

    public static String generate() {
        byte[] verifierBytes = new byte[6];
        random.nextBytes(verifierBytes);
        char[] chars = new char[verifierBytes.length];
        for (int i = 0; i < verifierBytes.length; i++) {
            chars[i] = DEFAULT_CODEC[random.nextInt(DEFAULT_CODEC.length)];
        }
        return String.valueOf(chars, 0, 3) + "-" + String.valueOf(chars, 3, 3);
    }

    public static String generateBackupCode() {
        byte[] verifierBytes = new byte[8];
        random.nextBytes(verifierBytes);
        char[] chars = new char[verifierBytes.length];
        for (int i = 0; i < verifierBytes.length; i++) {
            chars[i] = NUMBERS[random.nextInt(NUMBERS.length)];
        }
        return String.valueOf(chars, 0, 4) + " " + String.valueOf(chars, 4, 4);

    }

    public static String generatePhoneVerification() {
        byte[] verifierBytes = new byte[6];
        random.nextBytes(verifierBytes);
        char[] chars = new char[verifierBytes.length];
        for (int i = 0; i < verifierBytes.length; i++) {
            chars[i] = NUMBERS[random.nextInt(NUMBERS.length)];
        }
        return String.valueOf(chars);

    }
}
