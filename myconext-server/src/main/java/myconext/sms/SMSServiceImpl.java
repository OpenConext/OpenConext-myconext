package myconext.sms;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("unchecked")
public class SMSServiceImpl implements SMSService {

    private static final Log LOG = LogFactory.getLog(SMSServiceImpl.class);

    private final String url;
    public final String route;
    private final String templateNl;
    private final String templateEn;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String bearer;


    @SneakyThrows
    public SMSServiceImpl(String url, String bearer, String route) {
        this.url = url;
        this.templateNl = IOUtils.toString(new ClassPathResource("sms/template_nl.txt").getInputStream(), Charset.defaultCharset());
        this.templateEn = IOUtils.toString(new ClassPathResource("sms/template_en.txt").getInputStream(), Charset.defaultCharset());
        this.route = route;
        this.bearer = bearer;

        LOG.info("Converters: " + restTemplate.getMessageConverters());

        restTemplate.getInterceptors().add((request, body, execution) -> {
            LOG.info(String.format("Sending SMS request to %s with headers %s and body:\n%s",
                    request.getURI(),
                    request.getHeaders(),
                    new String(body, StandardCharsets.UTF_8)));
            return execution.execute(request, body);
        });
    }

    protected String formatMessage(String code, Locale locale) {
        String template = locale != null && "nl".equalsIgnoreCase(locale.getLanguage()) ? templateNl : templateEn;
        return String.format(template, code);
    }

    @Override
    public String send(String mobile, String code, Locale locale) {
        String format = formatMessage(code, locale);

        Map<String, Object> body = new HashMap<>();
        body.put("encoding", "auto");
        body.put("body", format);
        body.put("route", route);
        body.put("originator", "eduID");
        body.put("recipients", Collections.singletonList(mobile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(this.bearer);

        RequestEntity<?> requestEntity = new RequestEntity<>(body, headers, HttpMethod.POST, URI.create(url));
        restTemplate.exchange(requestEntity, Void.class);
        return format;
    }
}