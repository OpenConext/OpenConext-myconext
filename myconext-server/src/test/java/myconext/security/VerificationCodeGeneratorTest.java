package myconext.security;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;


public class VerificationCodeGeneratorTest {

    private final Pattern pattern = Pattern.compile("^[A-NP-Z1-9]{3}-[A-NP-Z0-9]{3}$");

    @Test
    public void generate() {
        String code = VerificationCodeGenerator.generate();
        assertTrue(pattern.matcher(code).matches());
    }
}