package myconext.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "services-configuration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicesConfiguration {

    private List<String> hideInOverview = new ArrayList<>();

}
