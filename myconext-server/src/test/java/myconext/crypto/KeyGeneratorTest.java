package myconext.crypto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyGeneratorTest {

    @Test
    public void oneWayHash() {
        String eppn = KeyGenerator.oneWayHash("1234567890@surfguest.nl");
        assertEquals(eppn, KeyGenerator.oneWayHash("1234567890@surfguest.nl"));
    }
}