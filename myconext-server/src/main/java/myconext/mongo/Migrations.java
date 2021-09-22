package myconext.mongo;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;

import myconext.manage.ServiceProviderResolver;
import myconext.model.EduID;
import myconext.model.PublicKeyCredentials;
import myconext.model.ServiceProvider;
import myconext.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@ChangeLog(order = "001")
public class Migrations {

    private static final Log LOG = LogFactory.getLog(Migrations.class);

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

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "003", id = "bugfixForDotReplacement", author = "okke.harsta@surf.nl")
    public void bugfixForDotReplacement(MongockTemplate mongoTemplate) {
        List<User> users = mongoTemplate.findAll(User.class, "users");
        users.forEach(user -> {
            this.mergeEduIDs(user);
            mongoTemplate.save(user);
        });
    }

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "004", id = "addServiceProviderInstitutionGuid", author = "okke.harsta@surf.nl")
    public void addServiceProviderInstitutionGuid(MongockTemplate mongoTemplate, ServiceProviderResolver serviceProviderResolver) {
        List<User> users = mongoTemplate.findAll(User.class, "users");
        serviceProviderResolver.refresh();
        users.forEach(user -> {
            user.getEduIDS().forEach(eduID -> {
                Optional<ServiceProvider> optionalServiceProvider = serviceProviderResolver.resolve(eduID.getServiceProviderEntityId());
                optionalServiceProvider.ifPresent(serviceProvider -> {
                    if (StringUtils.hasText(serviceProvider.getInstitutionGuid())) {
                        eduID.updateServiceProvider(serviceProvider);
                        mongoTemplate.save(user);
                        LOG.info("Added  institutionGuid to " + eduID.getServiceProviderEntityId());
                    }
                });
            });
        });
    }

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "005", id = "addTrackingGuid", author = "okke.harsta@surf.nl")
    public void addTrackingGuid(MongockTemplate mongoTemplate) {
        List<User> users = mongoTemplate.findAll(User.class, "users");
        users.forEach(user -> {
            if (!StringUtils.hasText(user.getTrackingUuid())) {
                user.setTrackingUuid(UUID.randomUUID().toString());
                mongoTemplate.save(user);
            }
        });
    }

    protected User mergeEduIDs(User user) {
        List<EduID> eduIDS = user.getEduIDS();
        //Make a copy to search in
        List<EduID> copiedEduIDs = new ArrayList<>(eduIDS);
        //First remove all the ones containing a "." and where the is an entry with the old format
        List<EduID> eduIDSFiltered = eduIDS.stream()
                .filter(eduID -> {
                    String entityId = eduID.getServiceProviderEntityId();
                    boolean remove = false;
                    boolean hasDot = entityId.contains(".");
                    if (hasDot) {
                        String oldEntityId = entityId.replaceAll("\\.", "@");
                        remove = copiedEduIDs.stream().anyMatch(someEduID -> someEduID.getServiceProviderEntityId().equals(oldEntityId));
                    }
                    if (remove) {
                        LOG.info("Removing " + eduID + " for user: " + user.getEmail());
                    }
                    return !remove;
                })
                .collect(Collectors.toList());
        eduIDSFiltered.forEach(eduID -> eduID.replaceAtWithDot());
        user.setEduIDS(eduIDSFiltered);
        return user;
    }
}
