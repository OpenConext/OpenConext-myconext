package myconext.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class VerificationCodeGeneratorTest {

    @Test
    public void generate() {
        String code = VerificationCodeGenerator.generate();
        assertEquals(7, code.length());
    }
}