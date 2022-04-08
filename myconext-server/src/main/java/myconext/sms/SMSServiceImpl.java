package myconext.sms;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SMSServiceImpl implements SMSService {

    private final String url;
    private final String template;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MultiValueMap<String, String> headers = new HttpHeaders();

    @SneakyThrows
    public SMSServiceImpl(String url, String bearer){
        this.url = url;
        this.template = IOUtil.toString(new ClassPathResource("sms/template.txt").getInputStream());
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer "+ bearer);
    }

    @Override
    public void send(String mobile, String code) {
        Map<String, Object> body = Map.of(
                "encoding", "auto",
                "body", String.format(template, code),
                "route", "business",
                "originator", "eduID",
                "recipients", List.of(mobile)
        );

        RequestEntity<?> requestEntity = new RequestEntity(body, headers, HttpMethod.POST, URI.create(url));
        restTemplate.exchange(requestEntity, Void.class);
    }
}