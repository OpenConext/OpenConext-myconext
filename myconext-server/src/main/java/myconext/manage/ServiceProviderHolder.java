package myconext.manage;

import myconext.model.SamlAuthenticationRequest;
import myconext.model.ServiceProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static myconext.security.CookieResolver.cookieByName;

public interface ServiceProviderHolder {

    ServiceProviderResolver getServiceProviderResolver() ;

    default String getServiceName(HttpServletRequest request, SamlAuthenticationRequest samlAuthenticationRequest) {
        String lang = cookieByName(request, "lang").map(cookie -> cookie.getValue()).orElse("en");
        Optional<ServiceProvider> optionalServiceProvider = getServiceProviderResolver().resolve(samlAuthenticationRequest.getRequesterEntityId());
        String serviceName = optionalServiceProvider.map(serviceProvider -> lang.equals("en") ? serviceProvider.getName() : serviceProvider.getNameNl())
                .orElse(samlAuthenticationRequest.getRequesterEntityId());
        return serviceName;
    }


}
