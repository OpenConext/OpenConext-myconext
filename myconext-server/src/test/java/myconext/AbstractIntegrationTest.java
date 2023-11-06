package myconext;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import myconext.api.MagicLinkResponse;
import myconext.manage.MockServiceProviderResolver;
import myconext.manage.ServiceProviderResolver;
import myconext.model.*;
import myconext.repository.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import tiqr.org.model.Authentication;
import tiqr.org.model.Enrollment;
import tiqr.org.model.Registration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static io.restassured.RestAssured.given;
import static myconext.security.GuestIdpAuthenticationRequestFilter.BROWSER_SESSION_COOKIE_NAME;
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
                "email_guessing_sleep_millis=1",
                "sp_entity_id=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "sp_entity_metadata_url=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "spring.main.lazy-initialization=true",
                "eduid_api.oidcng_introspection_uri=http://localhost:8098/introspect",
                "cron.service-name-resolver-initial-delay-milliseconds=60000",
                "oidc.base-url=http://localhost:8098/",
                "sso_mfa_duration_seconds=-1000"
        })
@ActiveProfiles({"test"})
@SuppressWarnings("unchecked")
public abstract class AbstractIntegrationTest {

    private static final ServiceProviderResolver serviceProviderResolver = new MockServiceProviderResolver();

    @LocalServerPort
    protected int port;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Qualifier("jsonMapper")
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected AuthenticationRequestRepository authenticationRequestRepository;

    @Autowired
    protected PasswordResetHashRepository passwordResetHashRepository;

    @Autowired
    protected MobileLinkAccountRequestRepository mobileLinkAccountRequestRepository;

    @Autowired
    protected ChangeEmailHashRepository changeEmailHashRepository;

    @Autowired
    protected EnrollmentRepository enrollmentRepository;

    @Autowired
    protected RegistrationRepository registrationRepository;

    @Autowired
    protected AuthenticationRepository authenticationRepository;

    @Autowired
    protected EmailsSendRepository emailsSendRepository;

    @Autowired
    protected RequestInstitutionEduIDRepository requestInstitutionEduIDRepository;

    private final SimpleDateFormat issueFormat = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss");

    protected final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    protected final Filter noopFilter = new NoopFilter();

    @Before
    @BeforeEach
    public void before() throws Exception {
        RestAssured.port = port;
        Arrays.asList(SamlAuthenticationRequest.class, User.class)
                .forEach(clazz -> mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, clazz)
                        .remove(new Query())
                        .insert(readFromFile(clazz))
                        .execute());
        Arrays.asList(PasswordResetHash.class, ChangeEmailHash.class, Challenge.class, EmailsSend.class,
                        Registration.class, Authentication.class, Enrollment.class, MobileLinkAccountRequest.class)
                .forEach(clazz -> mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, clazz)
                        .remove(new Query())
                        .execute());
    }

    protected String samlAuthnRequest() throws IOException {
        return samlAuthnRequest(null);
    }

    protected String samlAuthnRequest(String relayState) throws IOException {
        Response response = samlAuthnRequestResponse(null, relayState);
        return extractAuthenticationRequestIdFromAuthnResponse(response);
    }

    protected String extractAuthenticationRequestIdFromAuthnResponse(Response response) {
        assertEquals(302, response.getStatusCode());

        String location = response.getHeader("Location");
        assertTrue(location.startsWith("http://localhost:3000/login/"));
        if (!location.contains("?")) {
            return location.substring(location.lastIndexOf("/") + 1);
        }
        return location.substring(location.lastIndexOf("/") + 1, location.lastIndexOf("?"));
    }

    protected Response samlAuthnRequestResponse(Cookie cookie, String relayState) throws IOException {
        return samlAuthnRequestResponseWithLoa(cookie, relayState, "");
    }

    protected MagicLinkResponse magicLinkRequest(User user, HttpMethod method) throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        return magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, StringUtils.hasText(user.getPassword())), method);
    }

    protected MagicLinkResponse magicLinkRequest(MagicLinkRequest linkRequest, HttpMethod method) {
        RequestSpecification requestSpecification = given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(linkRequest);

        String path = "/myconext/api/idp/magic_link_request";
        Response response = method.equals(HttpMethod.POST) ? requestSpecification.post(path) : requestSpecification.put(path);
        return new MagicLinkResponse(linkRequest.getAuthenticationRequestId(), response.then());
    }

    protected Response samlAuthnRequestResponseWithLoa(Cookie cookie, String relayState, String loaLevel) throws IOException {
        String samlRequestTemplate = readFile("authn_request.xml");
        String samlRequest = String.format(samlRequestTemplate, UUID.randomUUID(), issueFormat.format(new Date()), loaLevel);
        String samlRequestEncoded = deflatedBase64encoded(samlRequest);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("SAMLRequest", samlRequestEncoded);
        if (StringUtils.hasText(relayState)) {
            queryParams.put("RelayState", relayState);
        }
        return given().redirects().follow(false)
                .when()
                .queryParams(queryParams)
                .cookie(cookie != null ? cookie : new Cookie.Builder("dummy", "dummy").build())
                .get("/saml/guest-idp/SSO");
    }

    public static String readFile(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path).getInputStream(), Charset.defaultCharset());
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
        Date expiresIn = Date.from(LocalDateTime.now().minusYears(10L).atZone(ZoneId.systemDefault()).toInstant());
        ReflectionTestUtils.setField(t, "expiresIn", expiresIn);
        mongoTemplate.save(t);
    }

    public User user(String email) {
        return user(email, "John", "Doe", "en");
    }

    public static User user(String email, String lang) {
        return user(email, "John", "Doe", lang);
    }

    public static User user(String email, String givenName, String familyName, String lang) {
        return new User(UUID.randomUUID().toString(), email, givenName, givenName, familyName, "surfguest.nl", lang,
                "http://mock-sp", serviceProviderResolver);
    }

    protected void userSetPassword(User user, String plainTextPassword) {
        ReflectionTestUtils.setField(user, "password", plainTextPassword);
    }

    protected String doOpaqueAccessToken(boolean valid, String[] scopes, String filePart) throws IOException {
        List<String> scopeList = new ArrayList<>(Arrays.asList(scopes));
        scopeList.add("openid");

        String file = String.format("oidc/%s.json", valid ? filePart : "introspect-invalid-token");
        String introspectResult = IOUtils.toString(new ClassPathResource(file).getInputStream(), Charset.defaultCharset());
        String introspectResultWithScope = valid ? String.format(introspectResult, String.join(" ", scopeList)) : introspectResult;
        stubFor(post(urlPathMatching("/introspect")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(introspectResultWithScope)));
        return UUID.randomUUID().toString();
    }

    protected String opaqueAccessTokenWithNoLinkedAccount(String... scopes) throws IOException {
        return doOpaqueAccessToken(true, scopes, "introspect_no_linked_accounts");
    }

    protected String opaqueAccessToken(boolean valid, String... scopes) throws IOException {
        return doOpaqueAccessToken(valid, scopes, "introspect");
    }

    protected void stubForTokenUserInfo(Map<Object, Object> userInfo) throws JsonProcessingException {
        stubFor(post(urlPathMatching("/oidc/token")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(Collections.singletonMap("access_token", "123456")))));
        stubFor(post(urlPathMatching("/oidc/userinfo")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(userInfo))));
    }

    protected Response get302Response(Response response, Optional<Filter> optionalCookieFilter, String queryParams) {
        //new user confirmation screen
        String uri = response.getHeader("Location");
        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(uri).build().getQueryParams();
        String h = parameters.getFirst("h");
        response = given().redirects().follow(false)
                .when()
                .queryParam("h", h)
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .filter(optionalCookieFilter.orElse(noopFilter))
                .get("/saml/guest-idp/magic" + queryParams);
        return response;
    }

    protected Response get302Response(Response response, Optional<Filter> optionalCookieFilter) {
        return get302Response(response, optionalCookieFilter, "");
    }

    protected String samlAuthnResponse(Response response, Optional<Filter> optionalCookieFilter) throws IOException {
        while (response.statusCode() == 302) {
            response = this.get302Response(response, optionalCookieFilter);
        }
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        String html = IOUtils.toString(response.asInputStream(), Charset.defaultCharset());

        Matcher matcher = Pattern.compile("name=\"SAMLResponse\" value=\"(.*?)\"").matcher(html);
        matcher.find();
        return new String(java.util.Base64.getDecoder().decode(matcher.group(1)));
    }

    protected Response magicResponse(MagicLinkResponse magicLinkResponse) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        Response response = given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");

        while (response.getStatusCode() == 302) {
            response = get302Response(response, Optional.empty());
        }
        return response;
    }


    protected String samlResponse(MagicLinkResponse magicLinkResponse) throws IOException {
        Response response = magicResponse(magicLinkResponse);

        return samlAuthnResponse(response, Optional.empty());
    }

}
