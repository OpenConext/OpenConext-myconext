package myconext.geo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoLocationTest {

    private final GeoLocation geoLocation = new GeoLocation(new ObjectMapper());

    @Test
    void findLocation() {
        assertEquals("Netherlands, Brielle", geoLocation.findLocation("145.90.230.172").get());
    }

    @Test
    void findLocationReservedRange() {
        assertTrue(geoLocation.findLocation("127.0.0.1").isEmpty());
    }
}