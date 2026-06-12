package myconext.api;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Hidden;
import myconext.model.IdpScoping;
import myconext.repository.MetricsRepository;
import myconext.repository.UserRepository;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@Hidden
public class MetricsController {

    public MetricsController(UserRepository userRepository,
                             MetricsRepository metricsRepository,
                             MeterRegistry meterRegistry) {
        Gauge.builder("user_count", () ->
                        userRepository.count())
                .description("User count")
                .register(meterRegistry);

        Gauge.builder("linked_account_count",
                        () -> metricsRepository.countTotalLinkedAccounts())
                .description("Internal linked account count")
                .register(meterRegistry);

        Gauge.builder("app_registration_count",
                        () -> metricsRepository.countTotalAppRegistrations())
                .description("App registration count")
                .register(meterRegistry);

        Gauge.builder("used_services_count",
                        () -> metricsRepository.countTotalUsedServices())
                .description("Used services count")
                .register(meterRegistry);

        Stream.of(IdpScoping.values())
                .forEach(idpScoping -> Gauge
                        .builder("external_linked_account_" + idpScoping.name(),
                                () -> metricsRepository.countTotalExternalLinkedAccountsByType(idpScoping))
                        .description("External account " + idpScoping.name() + " count ")
                        .register(meterRegistry));
    }

}
