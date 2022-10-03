package myconext.geo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhoIsGeoLocationTest {

    private final WhoIsGeoLocation geoLocation = new WhoIsGeoLocation(new ObjectMapper());

    @Test
    void findLocation() {
        assertEquals("Netherlands, Brielle", geoLocation.findLocation("145.90.230.172").get());
    }

    @Test
    void findLocationReservedRange() {
        assertTrue(geoLocation.findLocation("127.0.0.1").isEmpty());
    }
}