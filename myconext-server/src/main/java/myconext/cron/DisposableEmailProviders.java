package myconext.cron;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.exceptions.DisposableEmailProviderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class DisposableEmailProviders {

    private static final Log LOG = LogFactory.getLog(DisposableEmailProviders.class);

    private final ObjectMapper objectMapper;
    private final boolean testEnvironment;
    private Set<String> disposableEmailProviders = new HashSet<>();
    private final boolean denyDisposableEmailProviders;
    private final TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<>() {
    };

    @Autowired
    public DisposableEmailProviders(ObjectMapper objectMapper,
                                    Environment environment,
                                    @Value("${feature.deny_disposable_email_providers}") boolean denyDisposableEmailProviders) {
        this.objectMapper = objectMapper;
        this.testEnvironment = environment.acceptsProfiles(Profiles.of("test"));
        this.denyDisposableEmailProviders = denyDisposableEmailProviders;
    }

    //We don't want this running during integration tests
    @Scheduled(initialDelay = 1L, fixedRate = 24L, timeUnit = TimeUnit.HOURS)
    @SuppressWarnings("unchecked")
    public void resolveIDisposableEmailProviders() {
        if (!denyDisposableEmailProviders) {
            return;
        }
        long start = System.currentTimeMillis();
        try {
            String remoteLocation = "https://raw.githubusercontent.com/7c/fakefilter/main/json/data.json";
            String localLocation = "email/fake_filter.json";
            Map<String, Object> emailProviders = this.testEnvironment ?
                    objectMapper.readValue(new ClassPathResource(localLocation).getInputStream(), mapTypeReference) :
                    objectMapper.readValue(new URL(remoteLocation), mapTypeReference);

            Map<String, Object> domains = (Map<String, Object>) emailProviders.get("domains");
            disposableEmailProviders = parseDisposableEmailProviders(domains);
            LOG.info(String.format("Resolved %s disposable email providers %s in %s ms",
                    disposableEmailProviders.size(), this.testEnvironment ? localLocation : remoteLocation, System.currentTimeMillis() - start));
        } catch (Exception e) {
            LOG.error("Error in resolveIDisposableEmailProviders", e);
        }
    }

    @SuppressWarnings("unchecked")
    private HashSet<String> parseDisposableEmailProviders(Map<String, Object> domains) {
        Set<Map.Entry<String, Object>> entries = domains.entrySet();
        HashSet<String> newDisposableEmailProviders = new HashSet<>();
        entries.forEach(entry -> {
                    String domain = entry.getKey();
                    Map<String, Object> provider = (Map<String, Object>) entry.getValue();
                    newDisposableEmailProviders.add(domain.toLowerCase());
                    newDisposableEmailProviders.add(((String) provider.get("provider")).toLowerCase());
                });
        return newDisposableEmailProviders;
    }

    public void verifyDisposableEmailProviders(String email) {
        if (!denyDisposableEmailProviders) {
            return;
        }
        if (disposableEmailProviders.isEmpty()) {
            resolveIDisposableEmailProviders();
        }
        int beginIndex = email.indexOf("@") + 1;
        String domainLowerCase = email.substring(beginIndex).toLowerCase();
        boolean disposableEmail = disposableEmailProviders.contains(domainLowerCase);
        if (disposableEmail) {
            throw new DisposableEmailProviderException();
        }
    }

}
