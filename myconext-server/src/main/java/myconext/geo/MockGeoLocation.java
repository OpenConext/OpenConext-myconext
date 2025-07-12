package myconext.geo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@ConditionalOnProperty(prefix = "geo_location", name = "service", havingValue = "mock")
@Component
public class MockGeoLocation implements GeoLocation {

    @Override
    public Optional<String> findLocation(String ipAddress) {
        return Optional.empty();
    }
}
