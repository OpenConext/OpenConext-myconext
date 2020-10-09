package myconext.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.exceptions.ForbiddenException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmailDomainGuard {

    private final boolean allowEnabled;
    private final Set<String> allowedDomains;

    public EmailDomainGuard(@Value("${feature.use_deny_allow_list.allow_enabled}") boolean allowEnabled,
                            @Value("${feature.use_deny_allow_list.allow_location}") Resource allowLocationResource,
                            @Qualifier("jsonMapper") ObjectMapper objectMapper) throws IOException {
        this.allowEnabled = allowEnabled;
        allowedDomains = allowEnabled ? objectMapper.readValue(allowLocationResource.getInputStream(), new TypeReference<List<String>>() {
        }).stream().map(String::toLowerCase).collect(Collectors.toSet()) : new HashSet<>();
    }

    public void enforceIsAllowed(String email) {
        if (allowEnabled) {
            String domain = email.substring(email.lastIndexOf("@") + 1);
            final String domainLowerCase = domain.toLowerCase();
            boolean allowed = allowedDomains.stream().anyMatch(name -> name.equals(domainLowerCase) || domainLowerCase.endsWith("." + name));
            if (!allowed) {
                throw new ForbiddenException();
            }
        }
    }

    public Set<String> getAllowedDomains() {
        return allowedDomains;
    }
}
