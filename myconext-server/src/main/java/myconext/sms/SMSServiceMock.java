package myconext.sms;

import org.apache.commons.io.IOUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SMSServiceMock implements SMSService{

    private final String template;

    public SMSServiceMock() throws IOException {
        this.template = IOUtil.toString(new ClassPathResource("sms/template.txt").getInputStream());
    }

    @Override
    public void send(String mobile, String code) {
        try {
            byte[] bytes = String.format(template, code).getBytes(StandardCharsets.UTF_8);
            File tempFile = File.createTempFile("javasms", ".html");
            FileCopyUtils.copy(bytes, tempFile);
            Runtime.getRuntime().exec("open " + tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
