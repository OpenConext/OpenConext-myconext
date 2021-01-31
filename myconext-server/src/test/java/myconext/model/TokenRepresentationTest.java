package myconext.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TokenRepresentationTest {

    @Test
    public void testToString() {
        assertEquals("TokenRepresentation(id=1, type=ACCESS)", new TokenRepresentation("1", TokenType.ACCESS).toString());
    }
}