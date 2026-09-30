package myconext.repository;

import myconext.AbstractIntegrationTest;
import myconext.model.ExternalLinkedAccount;
import myconext.model.IdpScoping;
import myconext.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

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

    @Test
    void countExternalLinkedAccountsByBrin() {
        assertEquals(Map.of(), metricsRepository.countExternalLinkedAccountsByBrin());

        User jdoe = userRepository.findOneUserByEmail("jdoe@example.com");
        ExternalLinkedAccount idinAccount = new ExternalLinkedAccount("subjectID", IdpScoping.idin, true);
        idinAccount.setBrinCodes(List.of("ST42"));
        jdoe.getExternalLinkedAccounts().add(idinAccount);
        userRepository.save(jdoe);

        User mdoe = userRepository.findOneUserByEmail("mdoe@example.com");
        ExternalLinkedAccount studielinkAccount = new ExternalLinkedAccount("subjectID2", IdpScoping.studielink, true);
        studielinkAccount.setBrinCodes(List.of("ST42", "AB12"));
        mdoe.getExternalLinkedAccounts().add(studielinkAccount);
        userRepository.save(mdoe);

        Map<String, Integer> countsByBrin = metricsRepository.countExternalLinkedAccountsByBrin();
        assertEquals(2, countsByBrin.get("ST42"));
        assertEquals(1, countsByBrin.get("AB12"));
    }

}