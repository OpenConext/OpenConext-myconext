package myconext.cron;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.exceptions.DisposableEmailProviderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Set<String> disposableEmailProviders = new HashSet<>();
    private final TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<>() {
    };

    @Autowired
    public DisposableEmailProviders(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    //We don't want this running during integration tests
    @Scheduled(initialDelay = 1L, fixedRate = 24L, timeUnit = TimeUnit.HOURS)
    @SuppressWarnings("unchecked")
    public void resolveIDisposableEmailProviders() {
        long start = System.currentTimeMillis();
        try {
            String location = "https://raw.githubusercontent.com/7c/fakefilter/main/json/data.json";
            Map<String, Object> emailProviders = objectMapper.readValue(new URL(location), mapTypeReference);
            disposableEmailProviders = ((Map<String, Object>) emailProviders.get("domains")).keySet();
            LOG.info(String.format("Resolved %s disposable email providers %s in %s ms",
                    disposableEmailProviders.size(), location, System.currentTimeMillis() - start));
        } catch (Exception e) {
            LOG.error("Error in resolveIDisposableEmailProviders", e);
        }
    }

    public void verifyDisposableEmailProviders(String email) {
        if (disposableEmailProviders.isEmpty()) {
            resolveIDisposableEmailProviders();
        }
        int beginIndex = email.indexOf("@") + 1;
        boolean disposableEmail = disposableEmailProviders.contains(email.substring(beginIndex).toLowerCase());
        if (disposableEmail) {
            throw new DisposableEmailProviderException();
        }
    }

}
