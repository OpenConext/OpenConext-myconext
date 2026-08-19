package tiqr.org;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {ManagementWebSecurityAutoConfiguration.class, SecurityAutoConfiguration.class})
public class TiqrApplication {

	public static void main(String[] args) {
		SpringApplication.run(TiqrApplication.class, args);
	}

}
