package myconext.geo;

import myconext.WireMockExtension;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.core.io.ClassPathResource;

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
        byte[] body = IOUtil.toByteArray(new ClassPathResource(path).getInputStream());
        stubFor(get(urlPathMatching("/maxmind"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        String s = System.getProperty("java.io.tmpdir");
        geoLocation = new MaxMindGeoLocation("lb53kEx9iVCuBcnV", "http://localhost:8382/maxmind", s + "/geo");
    }

    @AfterEach
    void after() throws IOException {
        String s = System.getProperty("java.io.tmpdir");
        FileUtils.forceDelete(s + "/geo");
    }

    @Test
    void findLocation() {
        String actual = geoLocation.findLocation("145.90.230.172").get();
        assertEquals("Netherlands, Brielle", actual);
    }

    @Test
    void findLocationReservedRange() {
        assertTrue(geoLocation.findLocation("127.0.0.1").isEmpty());
    }
}