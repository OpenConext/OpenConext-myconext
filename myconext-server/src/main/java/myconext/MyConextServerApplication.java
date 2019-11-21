package myconext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, MetricsAutoConfiguration.class })
public class MyConextServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyConextServerApplication.class, args);
    }

}
