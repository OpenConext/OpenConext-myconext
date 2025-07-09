package myconext.api;

import io.swagger.v3.oas.annotations.Hidden;
import myconext.repository.MetricsRepository;
import myconext.repository.UserRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
public class MetricsController {

    private final UserRepository userRepository;
    private final MetricsRepository metricsRepository;

    public MetricsController(UserRepository userRepository, MetricsRepository metricsRepository) {
        this.userRepository = userRepository;
        this.metricsRepository = metricsRepository;
    }
}
