package myconext.api;

import myconext.exceptions.DuplicateUserEmailException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class DefaultErrorControllerTest {

    private DefaultErrorController subject;

    @Before
    public void before() {
        subject = new DefaultErrorController(new DefaultErrorAttributes());
    }

    @Test
    public void noError() throws URISyntaxException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity responseEntity = subject.error(request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertEquals("None", body.get("error"));
    }

    @Test
    public void errorAnnotated() throws URISyntaxException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR",
                new DuplicateUserEmailException("There already exists a user with email test@test.com"));

        ResponseEntity responseEntity = subject.error(request);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertEquals(409, body.get("status"));
        assertEquals("myconext.exceptions.DuplicateUserEmailException", body.get("exception"));
    }

    @Test
    public void errorNotAnnotated() throws URISyntaxException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR",
                new IllegalArgumentException("dope"));
        request.setAttribute("jakarta.servlet.error.status_code", 409);

        ResponseEntity responseEntity = subject.error(request);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertEquals(409, body.get("status"));
    }

}