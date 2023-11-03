package myconext.geo;

import myconext.WireMockExtension;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxMindGeoLocationTest {

    @RegisterExtension
    WireMockExtension mockServer = new WireMockExtension(8382);

    private GeoLocation geoLocation;

    @BeforeEach
    void before() throws IOException {
        String path = "/geo/GeoLite2-City_20220101.tar.gz";
        byte[] body = IOUtils.toByteArray(new ClassPathResource(path).getInputStream());
        stubFor(get(urlPathMatching("/maxmind"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        String s = System.getProperty("java.io.tmpdir");
        geoLocation = new MaxMindGeoLocation("nope", "http://localhost:8382/maxmind", s + "/geo");
    }

    @AfterEach
    void after() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/geo");
        FileUtils.forceDelete(file);
    }

    @Test
    void findLocation() {
        String actual = geoLocation.findLocation("145.90.230.172").get();
        assertEquals("Netherlands, Brielle", actual);

        //Test that when there is a valid database that no refresh is done
        stubFor(get(urlPathMatching("/maxmind"))
                .willReturn(aResponse()
                        .withStatus(500)));
        String s = System.getProperty("java.io.tmpdir");
        geoLocation = new MaxMindGeoLocation("nope", "http://localhost:8382/maxmind", s + "/geo");

        actual = geoLocation.findLocation("145.90.230.172").get();
        assertEquals("Netherlands, Brielle", actual);
        assertTrue(geoLocation.findLocation("127.0.0.1").isEmpty());
    }

}