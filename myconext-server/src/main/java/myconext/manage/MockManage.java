package myconext.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import myconext.model.IdentityProvider;
import myconext.model.ServiceProvider;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MockManage implements Manage {

    private final Map<String, ServiceProvider> serviceProviders;
    private final Map<String, IdentityProvider> identityProviders;

    @SneakyThrows
    public MockManage(ObjectMapper objectMapper) {
        List<Map<String, Object>> manageServices = new ArrayList<>();
        Stream.of("saml20_sp", "oidc10_rp").forEach(type ->
                manageServices.addAll(convertMaps(objectMapper, "/manage/" + type + ".json"))
        );
        serviceProviders = manageServices.stream().collect(Collectors.toMap(this::entityId, this::serviceProvider));
        List<Map<String, Object>> manageIdentityProviders = convertMaps(objectMapper, "/manage/saml20_idp.json");
        identityProviders = mergeByDomainNames(manageIdentityProviders);
    }

    @SneakyThrows
    private List<Map<String, Object>> convertMaps(ObjectMapper objectMapper, String type) {
        return objectMapper.readValue(IOUtils.toString(new ClassPathResource(type).getInputStream(), Charset.defaultCharset()),
                new TypeReference<>() {
                });
    }

    @Override
    public Optional<ServiceProvider> findServiceProviderByEntityId(String entityId) {
        return Optional.ofNullable(serviceProviders.get(entityId));
    }

    @Override
    public Set<String> getDomainNames() {
        return identityProviders.keySet();
    }

    @Override
    public Optional<IdentityProvider> findIdentityProviderByDomainName(String domainName) {
        return Optional.ofNullable(this.identityProviders.get(domainName));
    }

    @Override
    public List<IdentityProvider> findIdentityProviderByBrinCode(String brinCode) {
        List<IdentityProvider> filteredIdentityProviders = this.identityProviders.values().stream()
                .filter(identityProvider -> brinCode.equals(identityProvider.getInstitutionBrin()))
                .toList();
        Map<String, IdentityProvider> map = filteredIdentityProviders.stream()
                .collect(Collectors.toMap(IdentityProvider::getEntityId, e -> e, (a, b) -> a));

        return new ArrayList<>(map.values());
    }

    @Override
    public Optional<IdentityProvider> findIdentityProviderByInstitutionGUID(String institutionGUID) {
        return this.identityProviders.values().stream()
                .filter(identityProvider -> institutionGUID.equals(identityProvider.getInstitutionGuid()))
                .findFirst();
    }
}
