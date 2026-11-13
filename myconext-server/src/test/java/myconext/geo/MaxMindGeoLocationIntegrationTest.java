package myconext.geo;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MaxMindGeoLocationIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8381);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

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

    @Test
    public void findLocationFromDownloadedDatabase() throws IOException {
        stubDownload(tarGz("GeoLite2-City_20240101/GeoLite2-City.mmdb", testDatabase()));

        MaxMindGeoLocation geoLocation = newGeoLocation();

        assertEquals("The Netherlands, Amsterdam", geoLocation.findLocation("145.90.230.172").get());
        assertEquals("The Netherlands, Amsterdam", geoLocation.findLocation("145.90.230.1").get());
        // Not in the (tiny) database
        assertFalse(geoLocation.findLocation("127.0.0.1").isPresent());
        assertFalse(geoLocation.findLocation("145.90.231.1").isPresent());
        // Not an IP address
        assertFalse(geoLocation.findLocation("not-an-ip.invalid").isPresent());
    }

    @Test
    public void refreshReplacesDatabase() throws IOException {
        stubDownload(tarGz("GeoLite2-City_20240101/GeoLite2-City.mmdb", testDatabase()));

        MaxMindGeoLocation geoLocation = newGeoLocation();
        geoLocation.refresh();

        assertEquals("The Netherlands, Amsterdam", geoLocation.findLocation("145.90.230.172").get());
        wireMockRule.verify(2, getRequestedFor(urlPathEqualTo("/download")));
    }

    @Test
    public void archiveWithoutDatabase() throws IOException {
        stubDownload(tarGz("GeoLite2-City_20240101/README.txt", "no database".getBytes(StandardCharsets.UTF_8)));

        MaxMindGeoLocation geoLocation = newGeoLocation();

        assertFalse(geoLocation.findLocation("145.90.230.172").isPresent());
    }

    private MaxMindGeoLocation newGeoLocation() throws IOException {
        return new MaxMindGeoLocation("nope",
                "http://localhost:8381/download",
                temporaryFolder.newFolder("geo2lite").getAbsolutePath());
    }

    private void stubDownload(byte[] body) {
        stubFor(get(urlPathEqualTo("/download")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/octet-stream")
                .withBody(body)));
    }

    private byte[] testDatabase() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/geo/GeoLite2-City-test.mmdb")) {
            return inputStream.readAllBytes();
        }
    }

    private byte[] tarGz(String entryName, byte[] content) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GzipCompressorOutputStream(bytes))) {
            TarArchiveEntry entry = new TarArchiveEntry(entryName);
            entry.setSize(content.length);
            tar.putArchiveEntry(entry);
            tar.write(content);
            tar.closeArchiveEntry();
        }
        return bytes.toByteArray();
    }

}
