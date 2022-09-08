package myconext.sms;

import lombok.SneakyThrows;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class SMSServiceMock extends SMSServiceImpl {

    private final Environment environment;

    public SMSServiceMock(Environment environment) {
        super(null, null);
        this.environment = environment;
    }

    @SneakyThrows
    @Override
    public String send(String mobile, String code, Locale locale) {
        String format = super.formatMessage(code, locale);
        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            byte[] bytes = format.getBytes(StandardCharsets.UTF_8);
            File tempFile = File.createTempFile("javasms", ".html");
            FileCopyUtils.copy(bytes, tempFile);
            Runtime.getRuntime().exec("open " + tempFile.getAbsolutePath());
        }
        return format;
    }
}
