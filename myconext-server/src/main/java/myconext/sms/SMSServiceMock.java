package myconext.sms;

import lombok.SneakyThrows;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class SMSServiceMock extends SMSServiceImpl {

    private final Environment environment;
    // The last code sent, read by the e2e tests (see E2eController)
    private final AtomicReference<String> lastCode = new AtomicReference<>();

    public SMSServiceMock(Environment environment) {
        super(null, null, null);
        this.environment = environment;
    }

    @SneakyThrows
    @Override
    public String send(String mobile, String code, Locale locale) {
        lastCode.set(code);
        String format = super.formatMessage(code, locale);
        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            byte[] bytes = format.getBytes(StandardCharsets.UTF_8);
            File tempFile = File.createTempFile("javasms", ".html");
            FileCopyUtils.copy(bytes, tempFile);
            Runtime.getRuntime().exec(new String[]{"open", tempFile.getAbsolutePath()});
        }
        return format;
    }

    public Optional<String> lastCode() {
        return Optional.ofNullable(lastCode.get());
    }
}
