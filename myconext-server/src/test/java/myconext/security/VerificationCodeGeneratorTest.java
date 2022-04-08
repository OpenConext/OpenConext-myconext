package myconext.security;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;


public class VerificationCodeGeneratorTest {

    private final Pattern pattern = Pattern.compile("^[A-NP-Z1-9]{3}-[A-NP-Z0-9]{3}$");

    private final Pattern patternBackupCode = Pattern.compile("^[0-9]{4} [0-9]{4}$");

    @Test
    public void generate() {
        String code = VerificationCodeGenerator.generate();
        assertTrue(pattern.matcher(code).matches());
    }

    @Test
    public void generateBackupCode() {
        String backupCode = VerificationCodeGenerator.generateBackupCode();
        assertTrue(patternBackupCode.matcher(backupCode).matches());
    }
}