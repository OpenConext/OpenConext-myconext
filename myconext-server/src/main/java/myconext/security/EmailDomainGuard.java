package myconext.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.exceptions.ForbiddenException;
import myconext.model.AllowedDomain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class EmailDomainGuard {

    private static final Log LOG = LogFactory.getLog(EmailDomainGuard.class);

    private final boolean allowEnabled;
    private final Set<AllowedDomain> allowedDomains;

    public EmailDomainGuard(@Value("${feature.use_deny_allow_list.allow_enabled}") boolean allowEnabled,
                            @Value("${feature.use_deny_allow_list.allow_location}") Resource allowLocationResource,
                            @Qualifier("jsonMapper") ObjectMapper objectMapper) throws IOException {
        this.allowEnabled = allowEnabled;
        this.allowedDomains = allowEnabled ? objectMapper.readValue(allowLocationResource.getInputStream(), new TypeReference<List<AllowedDomain>>() {
                }).stream()
                .map(AllowedDomain::toLowerCase)
                .collect(Collectors.toSet()) : new HashSet<>();

        LOG.info(String.format("Parsed %s allowed domain names from resource %s. Whitelist is %s",
                allowedDomains.size(), allowLocationResource.getDescription(), allowEnabled ? "enabled" : "disabled"));
    }

    public void enforceIsAllowed(String email) {
        if (allowEnabled) {
            String domainName = domainName(email);
            boolean allowed = allowedDomains.stream().anyMatch(allowedDomainPredicate(domainName));
            if (!allowed) {
                throw new ForbiddenException();
            }
        }
    }

    public String schacHomeOrganizationByDomain(String schacHomeOrganization, String email) {
        if (!allowEnabled) {
            return schacHomeOrganization;
        }
        String domainName = domainName(email);
        LOG.info(String.format("Starting to lookup schacHomeOrganization for email domain %s", domainName));

        Optional<String> s = allowedDomains.stream().filter(allowedDomainPredicate(domainName))
                .findFirst().map(AllowedDomain::getSchacHomeOrganization);
        LOG.info(String.format("Returning schacHomeOrganization %s for email domain %s", s, domainName));

        return s.orElse(schacHomeOrganization);
    }

    public Set<String> getAllowedDomains() {
        return allowedDomains.stream().map(AllowedDomain::getEmailDomain).collect(Collectors.toSet());
    }

    private String domainName(String email) {
        return email.substring(email.lastIndexOf("@") + 1).trim().toLowerCase();
    }

    private Predicate<AllowedDomain> allowedDomainPredicate(String domain) {
        return allowedDomain -> allowedDomain.getEmailDomain().equals(domain) || domain.endsWith("." + allowedDomain.getEmailDomain());
    }

}
