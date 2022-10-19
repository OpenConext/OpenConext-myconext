package myconext.geo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxMindGeoLocationTest {

    private static GeoLocation geoLocation;

    @BeforeAll
    static void beforeAll() throws IOException {
        //TODO use @WireMockTest(httpPort = 8080)
        String path = "/geo/GeoLite2-City_20220101.tar.gz";
        stubFor(get(urlPathMatching("/tokens"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(IOUtil.toByteArray(new ClassPathResource(path).getInputStream())));
        String s = System.getProperty("java.io.tmpdir");
        File dir = new File(s + "/maxmind");
        dir.mkdirs();
        File out = new File(s + "/maxmind/GeoLite2-City_20220101.tar.gz");
        IOUtil.copy(new ClassPathResource(path).getInputStream(), new FileOutputStream(out));
        geoLocation = new MaxMindGeoLocation("lb53kEx9iVCuBcnV", ""+out.getAbsolutePath(), s + "geo");
    }

    @AfterAll
    static void afterAll() throws IOException {
        String s = System.getProperty("java.io.tmpdir");
        FileUtils.forceDelete(s + "/geo");
    }

    @Test
    void findLocation() {
        assertEquals("Netherlands, Brielle", geoLocation.findLocation("145.90.230.172").get());
    }

    @Test
    void findLocationReservedRange() {
        assertTrue(geoLocation.findLocation("127.0.0.1").isEmpty());
    }
}