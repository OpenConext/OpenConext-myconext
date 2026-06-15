package myconext.repository;

import myconext.AbstractIntegrationTest;
import myconext.model.IdpScoping;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MetricsRepository metricsRepository;

    @Test
    void getTotalLinkedAccountCount() {
        Integer totalLinkedAccountCount = metricsRepository.countTotalLinkedAccounts();
        assertEquals(2, totalLinkedAccountCount);
        Integer idinExternalAccounts = metricsRepository.countTotalExternalLinkedAccountsByType(IdpScoping.idin);
        assertEquals(0, idinExternalAccounts);
        Integer totalExternalLinkedAccounts = metricsRepository.countTotalExternalLinkedAccounts();
        assertEquals(0, totalExternalLinkedAccounts);
        Integer countTotalUsedServices = metricsRepository.countTotalUsedServices();
        assertEquals(2, countTotalUsedServices);
    }

}