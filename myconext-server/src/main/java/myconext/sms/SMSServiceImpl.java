package myconext.sms;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SMSServiceImpl implements SMSService {

    private final String url;
    private final String templateNl;
    private final String templateEn;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MultiValueMap<String, String> headers = new HttpHeaders();

    @SneakyThrows
    public SMSServiceImpl(String url, String bearer){
        this.url = url;
        this.templateNl = IOUtil.toString(new ClassPathResource("sms/template_nl.txt").getInputStream());
        this.templateEn = IOUtil.toString(new ClassPathResource("sms/template_en.txt").getInputStream());
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer "+ bearer);
    }

    protected String formatMessage(String code, Locale locale) {
        String template = locale != null && locale.getLanguage().equalsIgnoreCase("nl") ? templateNl : templateEn;
        return String.format(template, code);
    }

    @Override
    public String send(String mobile, String code, Locale locale) {
        String format = formatMessage(code, locale);
        Map<String, Object> body = Map.of(
                "encoding", "auto",
                "body", format,
                "route", "business",
                "originator", "eduID",
                "recipients", List.of(mobile)
        );

        RequestEntity<?> requestEntity = new RequestEntity(body, headers, HttpMethod.POST, URI.create(url));
        restTemplate.exchange(requestEntity, Void.class);
        return format;
    }
}