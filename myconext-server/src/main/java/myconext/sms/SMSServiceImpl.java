package myconext.sms;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SMSServiceImpl implements SMSService {

    private final String url;
    public final String route;
    private final String templateNl;
    private final String templateEn;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MultiValueMap<String, String> headers = new HttpHeaders();

    @SneakyThrows
    public SMSServiceImpl(String url, String bearer, String route) {
        this.url = url;
        this.templateNl = IOUtils.toString(new ClassPathResource("sms/template_nl.txt").getInputStream(), Charset.defaultCharset());
        this.templateEn = IOUtils.toString(new ClassPathResource("sms/template_en.txt").getInputStream(), Charset.defaultCharset());
        this.route = route;
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
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
                "route", route,
                "originator", "eduID",
                "recipients", List.of(mobile)
        );

        RequestEntity<?> requestEntity = new RequestEntity(body, headers, HttpMethod.POST, URI.create(url));
        restTemplate.exchange(requestEntity, Void.class);
        return format;
    }
}