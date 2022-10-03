package myconext.geo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

@Component
public class WhoIsGeoLocation implements GeoLocation {

    private static final Log LOG = LogFactory.getLog(WhoIsGeoLocation.class);

    private final ObjectMapper objectMapper;

    private final TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
    };

    public WhoIsGeoLocation(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    //10,000 per month
    @Override
    public Optional<String> findLocation(String ipAddress) {
        try {
            Map<String, Object> geoInfo = objectMapper.readValue(new URL(
                    String.format("https://ipwho.is/%s?fields=country,city,success,message", ipAddress)), typeReference);
            if ((boolean) geoInfo.get("success")) {
                return Optional.of(String.format("%s, %s", geoInfo.get("country"), geoInfo.get("city")));
            }
            LOG.warn("Exception from ipwho.is: " + geoInfo);
            return Optional.empty();
        } catch (IOException e) {
            LOG.warn("Exception from ipwho.is", e);
            return Optional.empty();
        }
    }
}
