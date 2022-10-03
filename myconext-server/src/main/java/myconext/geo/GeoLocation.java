package myconext.geo;

import java.util.Optional;

public interface GeoLocation {

    Optional<String> findLocation(String ipAddress);
}
