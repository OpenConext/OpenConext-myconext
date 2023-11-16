package myconext.manage;

import myconext.model.SamlAuthenticationRequest;
import myconext.model.ServiceProvider;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static myconext.security.CookieResolver.cookieByName;

public interface ServiceProviderHolder {

    ServiceProviderResolver getServiceProviderResolver() ;

    default String getServiceName(HttpServletRequest request, SamlAuthenticationRequest samlAuthenticationRequest) {
        String lang = cookieByName(request, "lang").map(Cookie::getValue).orElse("en");
        String requesterEntityId = samlAuthenticationRequest.getRequesterEntityId();
        Optional<ServiceProvider> optionalServiceProvider = getServiceProviderResolver().resolve(requesterEntityId);
        return optionalServiceProvider.map(serviceProvider -> lang.equals("en") ? serviceProvider.getName() : serviceProvider.getNameNl())
                .orElse(requesterEntityId);
    }


}
