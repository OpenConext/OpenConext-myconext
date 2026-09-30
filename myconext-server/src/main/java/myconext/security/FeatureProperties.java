package myconext.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "feature")
public class FeatureProperties {

    private List<String> forceGlobalUidEntities = new ArrayList<>();
}
