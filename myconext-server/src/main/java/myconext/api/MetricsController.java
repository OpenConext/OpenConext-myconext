package myconext.api;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import io.swagger.v3.oas.annotations.Hidden;
import myconext.model.IdpScoping;
import myconext.repository.MetricsRepository;
import myconext.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@RestController
@Hidden
public class MetricsController {

    private final MetricsRepository metricsRepository;
    private final MultiGauge externalLinkedAccountsByBrin;

    public MetricsController(UserRepository userRepository,
                             MetricsRepository metricsRepository,
                             MeterRegistry meterRegistry) {
        this.metricsRepository = metricsRepository;

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

        Gauge.builder("total_external_linked_account_count",
                        () -> metricsRepository.countTotalExternalLinkedAccounts())
                .description("Total external linked account count")
                .register(meterRegistry);

        Stream.of(IdpScoping.values())
                .forEach(idpScoping -> Gauge
                        .builder("external_linked_account_" + idpScoping.name(),
                                () -> metricsRepository.countTotalExternalLinkedAccountsByType(idpScoping))
                        .description("External account " + idpScoping.name() + " count ")
                        .register(meterRegistry));

        //BRIN codes are data-driven, so unlike IdpScoping we can't register a Gauge per known value up front
        this.externalLinkedAccountsByBrin = MultiGauge.builder("external_linked_account_count_by_brin")
                .description("External linked account count by BRIN")
                .register(meterRegistry);
        this.refreshExternalLinkedAccountsByBrin();
    }

    @Scheduled(initialDelay = 1L, fixedRate = 15L, timeUnit = TimeUnit.MINUTES)
    public void refreshExternalLinkedAccountsByBrin() {
        this.externalLinkedAccountsByBrin.register(
                metricsRepository.countExternalLinkedAccountsByBrin().entrySet().stream()
                        .map(entry -> MultiGauge.Row.of(Tags.of("brin", entry.getKey()), entry.getValue()))
                        .toList(),
                true);
    }

}
