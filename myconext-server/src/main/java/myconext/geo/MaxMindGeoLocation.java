package myconext.geo;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class MaxMindGeoLocation implements GeoLocation {

    private static final Log LOG = LogFactory.getLog(MaxMindGeoLocation.class);
    public static final String GEO_LITE_2_CITY = "geolite2_city_";
    public static final String GEO_LITE_2_CITY_MMDB = "geolite2_city.mmdb";

    private final String licenseKey;
    private final String downloadDirectory;
    private final String urlTemplate;
    private final String urlTemplateForLogging;

    // Can't be final as we need to swap references to read new databases
    private DatabaseReader databaseReader;

    @SneakyThrows
    public MaxMindGeoLocation(@Value("${geo_location.license_key}") String licenseKey,
                              @Value("${geo_location.external_url}") String externalUrl,
                              @Value("${geo_location.download_directory}") String downloadDirectory) {
        this.licenseKey = licenseKey;
        this.urlTemplateForLogging = externalUrl;
        this.urlTemplate = externalUrl.replaceFirst("\\{license_key\\}", licenseKey);
        this.downloadDirectory = downloadDirectory;
        File downloadDir = new File(downloadDirectory);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        InputStream inputStream = new FileInputStream(this.latestDownloadBinary(true));
        this.databaseReader = new DatabaseReader.Builder(inputStream).withCache(new CHMCache()).build();
    }

    private File latestDownloadBinary(boolean refresh) {
        long start = System.currentTimeMillis();
        LOG.info("Locating latest download binary geo2lite database");

        SortedSet<File> modificationOrder = new TreeSet<>((a, b) -> (int) (a.lastModified() - b.lastModified()));
        File[] files = new File(this.downloadDirectory).listFiles((dir, name) -> dir.isDirectory() && name.startsWith(GEO_LITE_2_CITY));
        if (checkFilePresence(refresh, files)) {
            return this.latestDownloadBinary(false);
        }
        modificationOrder.addAll(Arrays.asList(files));
        File last = modificationOrder.last();
        File[] databaseBinary = last.listFiles((dir, name) -> name.equals(GEO_LITE_2_CITY_MMDB));
        if (checkFilePresence(refresh, databaseBinary)) {
            return this.latestDownloadBinary(false);
        }
        LOG.info(String.format("Located latest download binary geo2lite database %s in %s ms",
                databaseBinary[0].getAbsolutePath(),
                System.currentTimeMillis() - start));

        return databaseBinary[0];
    }

    private boolean checkFilePresence(boolean refresh, File[] files) {
        if (files == null || files.length == 0) {
            if (refresh) {
                this.refresh();
                return true;
            } else {
                throw new IllegalArgumentException("Unable to locate or download latest geo_lite_2 file");
            }
        }
        return false;
    }

    @Override
    public Optional<String> findLocation(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            CityResponse city = databaseReader.city(inetAddress);
            return Optional.of(String.format("%s, %s", city.getCountry().getName(), city.getCity().getName()));
        } catch (IOException | GeoIp2Exception e) {
            return Optional.empty();
        }
    }

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.DAYS, initialDelay = 1L)
    public void refresh() {
        LOG.info("Starting to refresh geo-lite2 database from " + this.urlTemplateForLogging);
        long start = System.currentTimeMillis();
        try {
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String name = String.format("%s%s", GEO_LITE_2_CITY, today);
            File dir = new File(String.format("%s/%s", this.downloadDirectory, name));
            if (dir.exists()) {
                FileUtils.deleteDirectory(dir);
            } else {
                FileUtils.forceMkdir(dir);
            }
            File file = new File(String.format("%s/%s/%s.tar.gz", this.downloadDirectory, name, GEO_LITE_2_CITY));
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            InputStream inputStream = new URL(String.format(urlTemplate, licenseKey)).openStream();
            IOUtil.copy(inputStream, fileOutputStream);

            TarArchiveInputStream fin = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
            ArchiveEntry entry;
            File binaryData = null;
            while ((entry = fin.getNextEntry()) != null) {
                if (entry.getName().endsWith("mmdb")) {
                    binaryData = new File(String.format("%s/%s/%s", this.downloadDirectory, name, GEO_LITE_2_CITY_MMDB));
                    try (OutputStream o = Files.newOutputStream(binaryData.toPath())) {
                        IOUtil.copy(fin, o);
                    }
                    break;
                }
            }
            if (binaryData == null) {
                throw new IllegalArgumentException("Could not find mmdb file in " + file);
            }
            this.databaseReader = new DatabaseReader.Builder(binaryData).withCache(new CHMCache()).build();
            LOG.info(String.format("Finished refreshing geo-lite2 database from %s in %s ms",
                    this.urlTemplateForLogging,
                    System.currentTimeMillis() - start));
        } catch (Exception e) {
            //we don't want to stop the scheduling
            LOG.error("Error in refreshing the max-mind database", e);
        }
    }
}