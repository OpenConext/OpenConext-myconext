package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Optional;

@NoArgsConstructor
@Getter
public class EduID {

    private String value;
    private String serviceName;
    private String serviceNameNl;
    private String serviceLogoUrl;
    private String serviceHomeUrl;
    private Date createdAt;

    public EduID(String value, String entityId, Optional<ServiceProvider> serviceProviderOptional) {
        this.value = value;
        this.createdAt = new Date();
        if (serviceProviderOptional.isPresent()) {
            ServiceProvider serviceProvider = serviceProviderOptional.get();
            serviceName = serviceProvider.getName();
            serviceNameNl = StringUtils.hasText(serviceProvider.getNameNl()) ? serviceProvider.getNameNl() : serviceName;
            serviceHomeUrl = serviceProvider.getHomeUrl();
            serviceLogoUrl = serviceProvider.getLogoUrl();
        } else {
            serviceName = entityId;
            serviceNameNl = entityId;
        }
    }

    public void updateServiceProvider(ServiceProvider serviceProvider) {
        serviceName = serviceProvider.getName();
        serviceNameNl = StringUtils.hasText(serviceProvider.getNameNl()) ? serviceProvider.getNameNl() : serviceName;
        serviceHomeUrl = serviceProvider.getHomeUrl();
        serviceLogoUrl = serviceProvider.getLogoUrl();
    }
}
