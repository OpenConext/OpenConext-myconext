package myconext.repository;

import myconext.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MetricsRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MetricsRepository metricsRepository;

    @Test
    void getTotalLinkedAccountCount() {
        Number totalLinkedAccountCount = metricsRepository.getTotalLinkedAccountCount();
        System.out.println(totalLinkedAccountCount);

        Number l = metricsRepository.countTotalLinkedAccountsNativeExecute();
        System.out.println(l);
    }

}