package myconext.geo;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

public class MaxMindGeoLocation implements GeoLocation {

    //1000 lookups a day are allowed
    private final int accountId = 773632;
    private final String licenseKey = "DElVe6eROlzxPnbe";

    private final WebServiceClient client = new WebServiceClient.Builder(accountId, licenseKey)
            .host("geolite.info").build();

    @Override
    public Optional<String> findLocation(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            CityResponse city = client.city(inetAddress);
            return Optional.of(String.format("%s, %s", city.getCountry().getName(), city.getCity().getName()));
        } catch (IOException | GeoIp2Exception e) {
            return Optional.empty();
        }
    }
}
