package myconext.cron;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import myconext.model.SamlAuthenticationRequest;
import myconext.repository.AuthenticationRequestRepository;

import java.util.Date;

@Component
public class ResourceCleaner {

    private static final Log LOG = LogFactory.getLog(ResourceCleaner.class);

    private AuthenticationRequestRepository authenticationRequestRepository;
    private boolean cronJobResponsible;

    @Autowired
    public ResourceCleaner(AuthenticationRequestRepository authenticationRequestRepository,
                           @Value("${cron.node-cron-job-responsible}") boolean cronJobResponsible) {
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.cronJobResponsible = cronJobResponsible;
    }

    @Scheduled(cron = "${cron.token-cleaner-expression}")
    public void clean() {
        if (!cronJobResponsible) {
            return;
        }
        Date now = new Date();
        info(SamlAuthenticationRequest.class, authenticationRequestRepository.deleteByExpiresInBeforeAndRememberMe(now, false));
    }

    private void info(Class clazz, long count) {
        LOG.info(String.format("Deleted %s instances of %s in cleanup", count, clazz));
    }
}
