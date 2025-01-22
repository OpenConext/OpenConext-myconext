package myconext.geo;

import lombok.SneakyThrows;
import myconext.AbstractIntegrationTest;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * We converted from a WireMock stub based test, to a full-fledged integration test. Mainly
 * because of <a href="https://github.com/wiremock/wiremock/issues/2651">an issue with WireMock</a>
 */
public class MaxMindGeoLocationTest extends AbstractIntegrationTest {

    @Value("${geo_location.download_directory}")
    protected String downloadDirectory;

    @Autowired
    protected GeoLocation geoLocation;

    @SneakyThrows
    @Test
    @Ignore
    public void findLocation() {
        String location = geoLocation.findLocation("145.90.230.172").get();
        assertEquals("The Netherlands, Amsterdam", location);
        assertTrue(geoLocation.findLocation("127.0.0.1").isEmpty());

        ((MaxMindGeoLocation) geoLocation).refresh();

        String actual = geoLocation.findLocation("145.90.230.172").get();
        assertEquals("The Netherlands, Amsterdam", actual);

        File file = new File(downloadDirectory);
        FileUtils.forceDelete(file);

    }

    @SneakyThrows
    @Test
    public void findMockLocation() {
        assertInstanceOf(MockGeoLocation.class, geoLocation);
        assertTrue(geoLocation.findLocation("127.0.0.1").isEmpty());
    }
}