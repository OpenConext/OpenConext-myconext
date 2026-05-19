package myconext.geo;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import myconext.WireMockExtension;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertFalse;

public class MaxMindGeoLocationIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8381);


    @Test
    public void download() {

        stubFor(get(urlPathMatching("/download")).willReturn(aResponse()
                .withStatus(400)));
        MaxMindGeoLocation geoLocation = new MaxMindGeoLocation(
                "nope", "http://localhost:8381/download", "/var/tmp/geo2lite"
        );
        Optional<String> location = geoLocation.findLocation("127.0.0.1");
        assertFalse(location.isPresent());
    }

}