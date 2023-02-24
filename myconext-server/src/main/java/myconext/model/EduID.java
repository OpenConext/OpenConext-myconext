package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor
@Getter
public class EduID implements Serializable {

    private String serviceProviderEntityId;
    @Indexed
    private String value;
    private String serviceName;
    private String serviceNameNl;
    private String serviceLogoUrl;
    private String serviceHomeUrl;
    private String serviceInstutionGuid;
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date createdAt;

    public EduID(String value, String serviceProviderEntityId, Optional<ServiceProvider> serviceProviderOptional) {
        this.value = value;
        this.serviceProviderEntityId = serviceProviderEntityId;
        this.createdAt = new Date();
        if (serviceProviderOptional.isPresent()) {
            ServiceProvider serviceProvider = serviceProviderOptional.get();
            serviceName = serviceProvider.getName();
            serviceNameNl = StringUtils.hasText(serviceProvider.getNameNl()) ? serviceProvider.getNameNl() : serviceName;
            serviceHomeUrl = serviceProvider.getHomeUrl();
            serviceLogoUrl = serviceProvider.getLogoUrl();
            serviceInstutionGuid = serviceProvider.getInstitutionGuid();
        } else {
            serviceName = serviceProviderEntityId;
            serviceNameNl = serviceProviderEntityId;
        }
    }

    @SneakyThrows
    public EduID(String serviceProviderEntityId, Map<String, Object> values) {
        this.serviceProviderEntityId = serviceProviderEntityId;
        this.value = (String) values.get("value");
        this.serviceName = (String) values.get("serviceName");
        this.serviceNameNl = (String) values.get("serviceNameNl");
        this.serviceLogoUrl = (String) values.get("serviceLogoUrl");
        this.serviceHomeUrl = (String) values.get("serviceHomeUrl");
        Object createdAt = values.get("createdAt");
        if (createdAt instanceof Date) {
            this.createdAt = (Date) createdAt;
        } else if (createdAt instanceof String) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            this.createdAt = formatter.parse((String) createdAt);
        } else {
            this.createdAt = new Date();
        }

    }

    public void updateServiceProvider(ServiceProvider serviceProvider) {
        serviceName = serviceProvider.getName();
        serviceNameNl = StringUtils.hasText(serviceProvider.getNameNl()) ? serviceProvider.getNameNl() : serviceName;
        serviceHomeUrl = serviceProvider.getHomeUrl();
        serviceLogoUrl = serviceProvider.getLogoUrl();
        serviceInstutionGuid = serviceProvider.getInstitutionGuid();
    }

    public void replaceAtWithDot() {
        if (this.serviceProviderEntityId.contains("@")) {
            this.serviceProviderEntityId = this.serviceProviderEntityId.replaceAll("@", ".");
        }
    }

    @Override
    public String toString() {
        return "EduID{" +
                "serviceProviderEntityId='" + serviceProviderEntityId + '\'' +
                ", value='" + value + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
