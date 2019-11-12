package surfid;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;
import surfid.model.SamlAuthenticationRequest;
import surfid.model.User;
import surfid.repository.AuthenticationRequestRepository;
import surfid.repository.UserRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Override the @ActiveProfiles annotation if you don't want to have mock SAML authentication
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mongodb_db=surf_id_test",
                "cron.node-cron-job-responsible=false",
                "sp_entity_id=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "sp_entity_metadata_url=https://engine.test.surfconext.nl/authentication/sp/metadata"
        })
@ActiveProfiles({"dev", "test"})
@SuppressWarnings("unchecked")
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected AuthenticationRequestRepository authenticationRequestRepository;

    private SimpleDateFormat issueFormat = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss");

    @Before
    public void before() throws Exception {
        RestAssured.port = port;
        Arrays.asList(SamlAuthenticationRequest.class, User.class)
                .forEach(clazz -> mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, User.class)
                        .remove(new Query())
                        .insert(readFromFile(clazz))
                        .execute());
    }

    protected String samlAuthnRequest() throws IOException {
        return samlAuthnRequest(null);
    }

    protected String samlAuthnRequest(String relayState) throws IOException {
        Response response = samlAuthnRequestResponse(null, relayState);
        assertEquals(302, response.getStatusCode());

        String location = response.getHeader("Location");
        assertTrue(location.startsWith("http://localhost:3000/login/"));

        return location.substring(location.lastIndexOf("/") + 1);
    }

    protected Response samlAuthnRequestResponse(Cookie cookie, String relayState) throws IOException {
        String samlRequestTemplate = IOUtils.toString(new ClassPathResource("authn_request.xml").getInputStream(), Charset.defaultCharset());
        String samlRequest = String.format(samlRequestTemplate, UUID.randomUUID().toString(), issueFormat.format(new Date()));
        String samlRequestEncoded = deflatedBase64encoded(samlRequest);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("SAMLRequest", samlRequestEncoded);
        if (StringUtils.hasText(relayState)) {
            queryParams.put("RelayState" ,relayState);
        }
        return given().redirects().follow(false)
                .when()
                .queryParams(queryParams)
                .cookie(cookie != null ? cookie : new Cookie.Builder("dummy", "dummy").build())
                .get("/saml/guest-idp/SSO");
    }

    private String deflatedBase64encoded(String input) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        DeflaterOutputStream deflaterStream = new DeflaterOutputStream(bytesOut, deflater);
        deflaterStream.write(input.getBytes(Charset.defaultCharset()));
        deflaterStream.finish();
        return new String(Base64.encodeBase64(bytesOut.toByteArray()));
    }

    private <T> List<T> readFromFile(Class<T> clazz) {
        try {
            String name = AnnotationUtils.findAnnotation(clazz, Document.class).collection() + ".json";
            return objectMapper.readValue(new ClassPathResource(name).getInputStream(), new TypeReference<List<T>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> void doExpireWithFindProperty(Class<T> clazz, String property, String value) {
        T t = mongoTemplate.findOne(Query.query(Criteria.where(property).is(value)), clazz);
        Date expiresIn = Date.from(LocalDateTime.now().minusYears(1L).atZone(ZoneId.systemDefault()).toInstant());
        ReflectionTestUtils.setField(t, "expiresIn", expiresIn);
        mongoTemplate.save(t);
    }
}
