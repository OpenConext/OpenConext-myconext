package myconext.mongo;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import myconext.model.EduID;
import myconext.model.PublicKeyCredentials;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ChangeLog(order = "001")
public class Migrations {

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "001", id = "transformUserPublicKeyCredentials", author = "okke.harsta@surf.nl")
    public void transformUserPublicKeyCredentials(MongockTemplate mongoTemplate) {
        List<Map> usersAsMaps = mongoTemplate.findAll(Map.class, "users");
        usersAsMaps.forEach(userAsMap -> {
            if (userAsMap.containsKey("publicKeyCredentials")) {
                Object o = userAsMap.get("publicKeyCredentials");
                if (o instanceof Map) {
                    Map<String, String> credentials = (Map<String, String>) userAsMap.get("publicKeyCredentials");
                    if (!credentials.containsKey("name")) {
                        List<PublicKeyCredentials> publicKeyCredentials = credentials.entrySet().stream()
                                .map(entry -> new PublicKeyCredentials(entry.getKey(), entry.getValue(),
                                        "key-" + UUID.randomUUID().toString()))
                                .collect(Collectors.toList());
                        userAsMap.put("publicKeyCredentials", publicKeyCredentials);
                        mongoTemplate.save(userAsMap, "users");
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "002", id = "transformEduIdPerServiceProvider", author = "okke.harsta@surf.nl")
    public void transformEduIdPerServiceProvider(MongockTemplate mongoTemplate) {
        List<Map> usersAsMaps = mongoTemplate.findAll(Map.class, "users");
        usersAsMaps.forEach(userAsMap -> {
            if (userAsMap.containsKey("eduIdPerServiceProvider")) {
                Map<String, Map<String, Object>> eduIdPerServiceProvider = (Map<String, Map<String, Object>>) userAsMap.get("eduIdPerServiceProvider");
                List<EduID> eduIDS = new ArrayList<>();
                eduIdPerServiceProvider.forEach((serviceProviderEntityId, values) -> {
                    eduIDS.add(new EduID(serviceProviderEntityId, values));
                });
                userAsMap.put("eduIDS", eduIDS);
                userAsMap.remove("eduIdPerServiceProvider");
                mongoTemplate.save(userAsMap, "users");
            }
        });
    }
}
