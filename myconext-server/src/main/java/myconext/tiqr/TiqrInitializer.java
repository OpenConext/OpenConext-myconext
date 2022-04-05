package myconext.tiqr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import tiqr.org.model.Service;

import java.io.IOException;

@Component
public class TiqrInitializer {

    private final TiqrConfiguration configuration;

    public TiqrInitializer(@Value("${tiqr_configuration}") Resource resource) throws IOException {
        this.configuration = new Yaml().loadAs(resource.getInputStream(), TiqrConfiguration.class);
    }
}
