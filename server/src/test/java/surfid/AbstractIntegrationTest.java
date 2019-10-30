package surfid;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import surfid.model.SamlAuthenticationRequest;
import surfid.model.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Override the @ActiveProfiles annotation if you don't want to have mock SAML authentication
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mongodb_db=surf_id_test",
                "cron.node-cron-job-responsible=false"
        })
@SuppressWarnings("unchecked")
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    private TypeReference<List<User>> userTypeReference = new TypeReference<List<User>>() {
    };

    @Before
    public void before() throws IOException {
        RestAssured.port = port;
        mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, User.class)
                .remove(new Query())
                .insert(objectMapper.readValue(new ClassPathResource("users.json").getInputStream(), userTypeReference))
                .execute();
        Arrays.asList(SamlAuthenticationRequest.class)
                .forEach(clazz -> mongoTemplate.remove(new Query(), clazz));
    }

    private <T> void doExpireWithFindProperty(Class<T> clazz, String property, String value) {
        T t = mongoTemplate.findOne(Query.query(Criteria.where(property).is(value)), clazz);
        Date expiresIn = Date.from(LocalDateTime.now().minusYears(1L).atZone(ZoneId.systemDefault()).toInstant());
        ReflectionTestUtils.setField(t, "expiresIn", expiresIn);
        mongoTemplate.save(t);
    }
}
