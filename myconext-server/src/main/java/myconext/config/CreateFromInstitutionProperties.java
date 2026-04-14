package myconext.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "create-from-institution")
public class CreateFromInstitutionProperties {

    private List<String> returnUrlAllowedDomains = new ArrayList<>();
}
