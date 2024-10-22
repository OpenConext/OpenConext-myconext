package myconext.mongo;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import myconext.manage.Manage;
import myconext.model.EduID;
import myconext.model.LinkedAccount;
import myconext.model.PublicKeyCredentials;
import myconext.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
        //Depended on removed code, we pass this will-never-run-again migration
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
    public void addServiceProviderInstitutionGuid(MongockTemplate mongoTemplate, Manage serviceProviderResolver) {
        //Depended on removed code, we pass this will-never-run-again migration
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

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "006", id = "deleteSession", author = "okke.harsta@surf.nl")
    public void deleteSession(MongockTemplate mongoTemplate) {
        mongoTemplate.remove(new Query(), "sessions");
    }

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "007", id = "expiresAtLinkedAccounts", author = "okke.harsta@surf.nl")
    public void expiresAtLinkedAccounts(MongockTemplate mongoTemplate) {
        Criteria criteria = Criteria.where("linkedAccounts").exists(true).type(JsonSchemaObject.Type.ARRAY).ne(new ArrayList<>());
        List<User> users = mongoTemplate.find(new Query(criteria), User.class, "users");
        users.forEach(user -> {
            user.getLinkedAccounts().forEach(linkedAccount -> {
                Date createdAt = linkedAccount.getCreatedAt();
                //Should not happen, but just to be safe
                if (createdAt == null) {
                    createdAt = new Date();
                    linkedAccount.setCreatedAt(createdAt);
                }
                Date expiresAt = Date.from(createdAt.toInstant().plus(2190, ChronoUnit.DAYS));
                linkedAccount.setExpiresAt(expiresAt);
            });
            mongoTemplate.save(user);
        });
    }

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "006", id = "deleteSessionAfterUserUpdate", author = "okke.harsta@surf.nl")
    public void deleteSessionAfterUserUpdate(MongockTemplate mongoTemplate) {
        mongoTemplate.remove(new Query(), "sessions");
    }

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "007", id = "migrateUsers", author = "okke.harsta@surf.nl")
    public void migrateUsers(MongockTemplate mongoTemplate) {
        Criteria criteria = Criteria.where("linkedAccounts").exists(true).type(JsonSchemaObject.Type.ARRAY).ne(new ArrayList<>());
        List<User> users = mongoTemplate.find(new Query(criteria), User.class, "users");
        users.forEach(user -> user.linkedAccountsSorted().stream()
                .filter(LinkedAccount::areNamesValidated)
                .findFirst()
                .ifPresent(linkedAccount -> {
                    user.setGivenName(linkedAccount.getGivenName());
                    user.setFamilyName(linkedAccount.getFamilyName());
                    user.setChosenName(user.getGivenName());
                    linkedAccount.setPreferred(true);
                    mongoTemplate.save(user);
                }));
    }

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "008", id = "deleteSessionAgain", author = "okke.harsta@surf.nl")
    public void deleteSessionAgain(MongockTemplate mongoTemplate) {
        mongoTemplate.remove(new Query(), "sessions");
    }

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "009", id = "deleteSessionOneMore", author = "okke.harsta@surf.nl")
    public void deleteSessionOneMore(MongockTemplate mongoTemplate) {
        mongoTemplate.remove(new Query(), "sessions");
    }

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "010", id = "bugfixForFaultyMigration", author = "okke.harsta@surf.nl")
    public void bugfixForFaultyMigration(MongockTemplate mongoTemplate) {
        List<User> users = mongoTemplate.findAll(User.class, "users");
        users.forEach(user -> {
            if (!CollectionUtils.isEmpty(user.getEduIDS())) {
                AtomicBoolean userNeedsUpdate = new AtomicBoolean(false);
                user.getEduIDS().forEach(eduID -> {
                    if (!StringUtils.hasText(eduID.getServiceProviderEntityId()) && !CollectionUtils.isEmpty(eduID.getServices()) ) {
                        eduID.backwardCompatibleTransformation(eduID.getServices().get(0));
                        userNeedsUpdate.set(true);
                    }
                });
                if (userNeedsUpdate.get()) {
                    mongoTemplate.save(user);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @ChangeSet(order = "011", id = "deleteSessionAfterUserChange", author = "okke.harsta@surf.nl")
    public void deleteSessionAfterUserChange(MongockTemplate mongoTemplate) {
        mongoTemplate.remove(new Query(), "sessions");
    }

    protected User mergeEduIDs(User user) {
        List<EduID> eduIDS = user.getEduIDS();
        //Make a copy to search in
        List<EduID> copiedEduIDs = new ArrayList<>(eduIDS);
        //First remove all the ones containing a "." and where there is an entry with the old format
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
