package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@NoArgsConstructor
@Getter
public class EduID implements Serializable {
    @Indexed
    private String value;

    //All following properties are obsolete and will be replaced - on the fly - with the List of ServiceProvider
    private String serviceProviderEntityId;
    private String serviceName;
    private String serviceNameNl;
    private String serviceLogoUrl;
    private String serviceHomeUrl;
    private String serviceInstutionGuid;

    //The new situation where services share the eduID value because of equals institution identifiers
    private List<ServiceProvider> services = new ArrayList<>();

    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date createdAt;

    public EduID(String value, ServiceProvider serviceProvider) {
        this.value = value;
        services.add(serviceProvider);
        this.createdAt = new Date();
    }

    public EduID updateServiceProvider(ServiceProvider serviceProvider) {
        //We migrate to the situation that en eduID only has a unique value and multiple services
        this.serviceProviderEntityId = null;
        this.serviceName = null;
        this.serviceNameNl = null;
        this.serviceLogoUrl = null;
        this.serviceHomeUrl = null;
        this.serviceInstutionGuid = null;

        Optional<ServiceProvider> optionalServiceProvider = this.services.stream()
                .filter(sp -> sp.getEntityId().equals(serviceProvider.getEntityId()))
                .findFirst();
        optionalServiceProvider.ifPresentOrElse(sp -> {
            sp.setName(serviceProvider.getName());
            sp.setNameNl(serviceProvider.getNameNl());
            sp.setHomeUrl(serviceProvider.getHomeUrl());
            sp.setLogoUrl(serviceProvider.getLogoUrl());
            sp.setInstitutionGuid(serviceProvider.getInstitutionGuid());
        }, () -> {
            this.services.add(serviceProvider);
        });
        return this;
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
