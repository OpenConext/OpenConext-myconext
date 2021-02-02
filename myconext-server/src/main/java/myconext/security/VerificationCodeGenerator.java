package myconext.security;

import java.security.SecureRandom;
import java.util.Random;

public class VerificationCodeGenerator {

    private static final char[] DEFAULT_CODEC = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            .toCharArray();

    private static final Random random = new SecureRandom();

    public static String generate() {
        byte[] verifierBytes = new byte[6];
        random.nextBytes(verifierBytes);
        char[] chars = new char[verifierBytes.length];
        for (int i = 0; i < verifierBytes.length; i++) {
            chars[i] = DEFAULT_CODEC[random.nextInt(DEFAULT_CODEC.length)];
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(chars,0,3);
        stringBuilder.append("-");
        stringBuilder.append(chars,3,3);
        return stringBuilder.toString();
    }

}
