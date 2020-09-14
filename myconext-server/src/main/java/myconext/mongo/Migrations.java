package myconext.mongo;

import com.github.mongobeej.changeset.ChangeLog;
import com.github.mongobeej.changeset.ChangeSet;
import myconext.model.PublicKeyCredentials;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ChangeLog
public class Migrations {

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "001", id = "transformUserPublicKeyCredentials", author = "okke.harsta@surf.nl")
    @Transactional
    public void transformUserPublicKeyCredentials(MongoTemplate mongoTemplate) {
        List<Map> usersAsMaps = mongoTemplate.findAll(Map.class, "users");
        usersAsMaps.forEach(userAsMap -> {
            if (userAsMap.containsKey("publicKeyCredentials")) {
                Object publicKeyCredentialsValue = userAsMap.get("publicKeyCredentials");
                if (publicKeyCredentialsValue instanceof Map) {
                    Map<String, String> credentials = (Map<String, String>) publicKeyCredentialsValue;
                    List<PublicKeyCredentials> publicKeyCredentials = credentials.entrySet().stream()
                            .map(entry -> new PublicKeyCredentials(entry.getKey(), entry.getValue(),
                                    "key-" + UUID.randomUUID().toString()))
                            .collect(Collectors.toList());
                    userAsMap.put("publicKeyCredentials", publicKeyCredentials);
                    mongoTemplate.save(userAsMap, "users");
                }
            }
        });
    }

}
