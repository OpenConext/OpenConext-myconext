package myconext.sms;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtil;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SMSServiceMock implements SMSService {

    private final String template;
    private final Environment environment;

    public SMSServiceMock(Environment environment) throws IOException {
        this.template = IOUtil.toString(new ClassPathResource("sms/template.txt").getInputStream());
        this.environment = environment;
    }

    @SneakyThrows
    @Override
    public void send(String mobile, String code) {
        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            byte[] bytes = String.format(template, code).getBytes(StandardCharsets.UTF_8);
            File tempFile = File.createTempFile("javasms", ".html");
            FileCopyUtils.copy(bytes, tempFile);
            Runtime.getRuntime().exec("open " + tempFile.getAbsolutePath());
        }
    }
}
