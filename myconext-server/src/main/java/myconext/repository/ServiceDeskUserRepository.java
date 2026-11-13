package myconext.repository;

import myconext.activity.ActivityContext;
import myconext.model.Activity;
import myconext.model.ActivityEvent;
import myconext.model.EduID;
import myconext.model.ExternalLinkedAccount;
import myconext.model.IdpScoping;
import myconext.model.LinkedAccount;
import myconext.model.ServiceProvider;
import myconext.model.ServiceDeskUser;
import myconext.model.ServiceDeskOverview;
import myconext.model.ProvisionedLinkedAccount;
import myconext.model.ServiceDeskUserDetail;
import myconext.model.ServiceDeskUserSearchResult;
import myconext.model.User;
import myconext.tiqr.SURFSecureID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Search for the users that have a linked account with the institution of a service desk member
 */
@Repository
public class ServiceDeskUserRepository {

    public static final int MAX_PAGE_SIZE = 100;

    private static final List<String> SEARCH_FIELDS = List.of("email", "givenName", "familyName", "chosenName", "uid");

    private final MongoTemplate mongoTemplate;

    public ServiceDeskUserRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param schacHomeOrganization only users with a (external) linked account for this institution are returned
     * @param queryString           the name, email or uid of the user, every word must match
     * @param mfaMethods            {@link ServiceDeskUser#MFA_APP} and / or {@link ServiceDeskUser#MFA_PASSKEY}, a user must have at least one
     * @param blocked               null for all users, otherwise only the blocked or the not blocked users
     * @param sortBy                name or email
     */
    public ServiceDeskUserSearchResult search(String schacHomeOrganization,
                                              String queryString,
                                              List<String> mfaMethods,
                                              Boolean blocked,
                                              String sortBy,
                                              boolean ascending,
                                              int page,
                                              int pageSize) {
        if (!StringUtils.hasText(schacHomeOrganization)) {
            //Never return all users
            return new ServiceDeskUserSearchResult(List.of(), 0, page, pageSize);
        }
        int size = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));
        int pageNumber = Math.max(0, page);

        List<Criteria> criteria = new ArrayList<>();
        criteria.add(institutionCriteria(schacHomeOrganization));
        criteria.addAll(searchCriteria(queryString));
        if (!CollectionUtils.isEmpty(mfaMethods)) {
            criteria.add(mfaCriteria(mfaMethods));
        }
        if (blocked != null) {
            criteria.add(blocked ? Criteria.where("blocked").is(true) : Criteria.where("blocked").ne(true));
        }
        Query query = new Query(new Criteria().andOperator(criteria))
                .collation(Collation.of("en").strength(2));

        long total = mongoTemplate.count(query, User.class);

        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = "email".equals(sortBy) ?
                Sort.by(direction, "email") :
                Sort.by(direction, "familyName", "givenName").and(Sort.by("email"));
        List<User> users = mongoTemplate.find(query.with(PageRequest.of(pageNumber, size, sort)), User.class);

        List<ServiceDeskUser> result = users.stream()
                .map(user -> toServiceDeskUser(user, schacHomeOrganization))
                .toList();
        return new ServiceDeskUserSearchResult(result, total, pageNumber, size);
    }

    private Criteria institutionCriteria(String schacHomeOrganization) {
        Pattern organization = exactIgnoreCase(schacHomeOrganization);
        return new Criteria().orOperator(
                Criteria.where("linkedAccounts").elemMatch(Criteria.where("schacHomeOrganization").regex(organization)),
                Criteria.where("externalLinkedAccounts").elemMatch(Criteria.where("schacHomeOrganization").regex(organization)));
    }

    private List<Criteria> searchCriteria(String queryString) {
        if (!StringUtils.hasText(queryString)) {
            return List.of();
        }
        return List.of(queryString.trim().split("\\s+")).stream()
                .map(word -> {
                    Pattern contains = Pattern.compile(Pattern.quote(word), Pattern.CASE_INSENSITIVE);
                    return new Criteria().orOperator(SEARCH_FIELDS.stream()
                            .map(field -> Criteria.where(field).regex(contains))
                            .toArray(Criteria[]::new));
                })
                .toList();
    }

    private Criteria mfaCriteria(List<String> mfaMethods) {
        List<Criteria> methods = new ArrayList<>();
        if (mfaMethods.contains(ServiceDeskUser.MFA_APP)) {
            methods.add(Criteria.where("surfSecureId." + SURFSecureID.PHONE_VERIFIED).exists(true));
            methods.add(Criteria.where("surfSecureId." + SURFSecureID.RECOVERY_CODE).exists(true));
        }
        if (mfaMethods.contains(ServiceDeskUser.MFA_PASSKEY)) {
            methods.add(Criteria.where("publicKeyCredentials.0").exists(true));
        }
        if (mfaMethods.contains(ServiceDeskUser.MFA_PASSWORD)) {
            methods.add(Criteria.where("password").exists(true).ne(null).ne(""));
        }
        //Unknown methods match nothing
        return methods.isEmpty() ? Criteria.where("_id").exists(false) :
                new Criteria().orOperator(methods.toArray(Criteria[]::new));
    }

    /**
     * The number of eduID accounts in time and the services they are logged into, for the institution
     */
    public ServiceDeskOverview overview(String schacHomeOrganization) {
        if (!StringUtils.hasText(schacHomeOrganization)) {
            return new ServiceDeskOverview(null, 0, List.of(), List.of());
        }
        Query query = new Query(institutionCriteria(schacHomeOrganization));
        query.fields().include("created").include("eduIDS");
        List<User> users = mongoTemplate.find(query, User.class);

        //Accounts without a creation date are counted in the baseline
        long baseline = users.stream().filter(user -> user.getCreated() <= 0).count();
        Map<YearMonth, Long> perMonth = new TreeMap<>();
        users.stream()
                .filter(user -> user.getCreated() > 0)
                .map(user -> YearMonth.from(Instant.ofEpochSecond(user.getCreated()).atZone(ZoneOffset.UTC)))
                .forEach(month -> perMonth.merge(month, 1L, Long::sum));

        String firstMonth = null;
        List<Long> added = new ArrayList<>();
        if (!perMonth.isEmpty()) {
            YearMonth first = ((TreeMap<YearMonth, Long>) perMonth).firstKey();
            YearMonth last = YearMonth.now(ZoneOffset.UTC);
            firstMonth = first.toString();
            for (YearMonth month = first; !month.isAfter(last); month = month.plusMonths(1)) {
                added.add(perMonth.getOrDefault(month, 0L));
            }
        }

        Map<String, Long> serviceUsers = new LinkedHashMap<>();
        for (User user : users) {
            //A user is counted once per service, even if multiple eduID's share the service
            user.getEduIDS().stream()
                    .flatMap(eduID -> eduID.getServices().stream())
                    .map(service -> StringUtils.hasText(service.getNameNl()) && !StringUtils.hasText(service.getName()) ?
                            service.getNameNl() : StringUtils.hasText(service.getName()) ? service.getName() : service.getEntityId())
                    .filter(StringUtils::hasText)
                    .distinct()
                    .forEach(name -> serviceUsers.merge(name, 1L, Long::sum));
        }
        List<ServiceDeskOverview.ServiceUsage> services = serviceUsers.entrySet().stream()
                .map(entry -> new ServiceDeskOverview.ServiceUsage(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(ServiceDeskOverview.ServiceUsage::users).reversed()
                        .thenComparing(ServiceDeskOverview.ServiceUsage::name))
                .toList();
        return new ServiceDeskOverview(firstMonth, baseline, added, services);
    }

    /**
     * @return the user, only if the user has a (external) linked account for the institution
     */
    public Optional<ServiceDeskUserDetail> findDetail(String id, String schacHomeOrganization) {
        if (!StringUtils.hasText(schacHomeOrganization) || !StringUtils.hasText(id)) {
            return Optional.empty();
        }
        Query query = new Query(new Criteria().andOperator(
                Criteria.where("_id").is(id), institutionCriteria(schacHomeOrganization)));
        User user = mongoTemplate.findOne(query, User.class);
        if (user == null) {
            return Optional.empty();
        }
        List<String> verifiedBy = user.getExternalLinkedAccounts().stream()
                .map(ExternalLinkedAccount::getIdpScoping)
                .filter(Objects::nonNull)
                .map(IdpScoping::name)
                .distinct()
                .toList();
        boolean namesVerified = user.getExternalLinkedAccounts().stream()
                .anyMatch(ExternalLinkedAccount::areNamesValidated);
        return Optional.of(new ServiceDeskUserDetail(user.getId(), user.getUid(), user.getEduPersonPrincipalName(),
                user.getEmail(), user.getDerivedGivenName(), user.getDerivedFamilyName(), user.getChosenName(),
                user.getDerivedDateOfBirth(), namesVerified, user.isBlocked(), mfaMethods(user),
                affiliations(user, schacHomeOrganization), verifiedBy, activities(user),
                institutionName(user, schacHomeOrganization, ProvisionedLinkedAccount::getGivenName),
                institutionName(user, schacHomeOrganization, ProvisionedLinkedAccount::getFamilyName)));
    }

    /**
     * The name as provided by the linked account of the user at the institution
     */
    private String institutionName(User user, String schacHomeOrganization,
                                   java.util.function.Function<ProvisionedLinkedAccount, String> name) {
        return Stream.concat(
                        user.getLinkedAccounts().stream()
                                .filter(account -> schacHomeOrganization.equalsIgnoreCase(account.getSchacHomeOrganization()))
                                .map(account -> (ProvisionedLinkedAccount) account),
                        user.getExternalLinkedAccounts().stream()
                                .filter(account -> schacHomeOrganization.equalsIgnoreCase(account.getSchacHomeOrganization()))
                                .map(account -> (ProvisionedLinkedAccount) account))
                .map(name)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    /**
     * Removes the MFA methods (the surfSecureId) of the user
     *
     * @param context the details of the activity that is recorded, e.g. the service desk member that reset the MFA
     * @return true if the user is found for the institution, false if the user was not found
     */
    public boolean resetMfa(String id, String schacHomeOrganization, Map<String, String> context) {
        if (!StringUtils.hasText(schacHomeOrganization) || !StringUtils.hasText(id)) {
            return false;
        }
        Query query = new Query(new Criteria().andOperator(
                Criteria.where("_id").is(id), institutionCriteria(schacHomeOrganization)));
        Activity activity = new Activity(id, ActivityEvent.MFA_RESET, new Date(), context);
        Update update = new Update().unset("surfSecureId")
                .push("activities").slice(-User.MAX_ACTIVITIES).each(activity);
        return mongoTemplate.updateFirst(query, update, User.class).getMatchedCount() > 0;
    }

    /**
     * @param context the details of the activity that is recorded, e.g. the service desk member that blocked the user
     * @return true if the user is found for the institution and blocked, false if the user was not found
     */
    public boolean block(String id, String schacHomeOrganization, Map<String, String> context) {
        return updateBlocked(id, schacHomeOrganization, true, ActivityEvent.BLOCKED_ACCOUNT, context);
    }

    /**
     * @param context the details of the activity that is recorded, e.g. the service desk member that deblocked the user
     * @return true if the user is found for the institution and deblocked, false if the user was not found
     */
    public boolean unblock(String id, String schacHomeOrganization, Map<String, String> context) {
        return updateBlocked(id, schacHomeOrganization, false, ActivityEvent.DEBLOCKED_ACCOUNT, context);
    }

    private boolean updateBlocked(String id, String schacHomeOrganization, boolean blocked, ActivityEvent event,
                                  Map<String, String> context) {
        if (!StringUtils.hasText(schacHomeOrganization) || !StringUtils.hasText(id)) {
            return false;
        }
        Query query = new Query(new Criteria().andOperator(
                Criteria.where("_id").is(id), institutionCriteria(schacHomeOrganization)));
        Activity activity = new Activity(id, event, new Date(), context);
        Update update = new Update().set("blocked", blocked)
                .push("activities").slice(-User.MAX_ACTIVITIES).each(activity);
        return mongoTemplate.updateFirst(query, update, User.class).getMatchedCount() > 0;
    }

    /**
     * The recorded activities and the logins at services, as derived from the eduID's of the user. Most recent first
     */
    private List<Activity> activities(User user) {
        List<Activity> activities = new ArrayList<>(user.allActivities());
        for (EduID eduID : user.getEduIDS()) {
            for (ServiceProvider service : eduID.getServices()) {
                Map<String, String> context = new LinkedHashMap<>();
                context.put(ActivityContext.SERVICE, StringUtils.hasText(service.getName()) ? service.getName() : service.getEntityId());
                if (StringUtils.hasText(service.getNameNl())) {
                    context.put(ActivityContext.SERVICE_NL, service.getNameNl());
                }
                Date first = service.getCreatedAt() != null ? service.getCreatedAt() : eduID.getCreatedAt();
                if (first != null) {
                    activities.add(new Activity(user.getId(), ActivityEvent.SERVICE_FIRST_LOGIN, first, context));
                }
                if (service.getLastLogin() != null && !service.getLastLogin().equals(first)) {
                    activities.add(new Activity(user.getId(), ActivityEvent.SERVICE_LAST_LOGIN, service.getLastLogin(), context));
                }
            }
        }
        activities.sort(Comparator.comparing(Activity::getDateTime, Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
        return activities;
    }

    private ServiceDeskUser toServiceDeskUser(User user, String schacHomeOrganization) {
        String name = String.join(" ",
                Stream.of(StringUtils.hasText(user.getChosenName()) ? user.getChosenName() : user.getGivenName(),
                                user.getFamilyName())
                        .filter(StringUtils::hasText)
                        .toList());
        return new ServiceDeskUser(user.getId(), user.getUid(), name, user.getEmail(), user.isBlocked(),
                mfaMethods(user), affiliations(user, schacHomeOrganization), List.of(),
                user.getEduPersonPrincipalName());
    }

    private List<String> mfaMethods(User user) {
        List<String> mfaMethods = new ArrayList<>();
        if (user.hasSecondFactor()) {
            mfaMethods.add(ServiceDeskUser.MFA_APP);
        }
        if (!CollectionUtils.isEmpty(user.getPublicKeyCredentials())) {
            mfaMethods.add(ServiceDeskUser.MFA_PASSKEY);
        }
        if (StringUtils.hasText(user.getPassword())) {
            mfaMethods.add(ServiceDeskUser.MFA_PASSWORD);
        }
        return mfaMethods;
    }

    private List<String> affiliations(User user, String schacHomeOrganization) {
        List<String> affiliations = new ArrayList<>();
        user.getLinkedAccounts().stream()
                .filter(account -> schacHomeOrganization.equalsIgnoreCase(account.getSchacHomeOrganization()))
                .map(LinkedAccount::getEduPersonAffiliations)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(affiliations::add);
        user.getExternalLinkedAccounts().stream()
                .filter(account -> schacHomeOrganization.equalsIgnoreCase(account.getSchacHomeOrganization()))
                .map(ExternalLinkedAccount::getAffiliations)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(affiliations::add);
        return affiliations.stream().distinct().toList();
    }

    private Pattern exactIgnoreCase(String value) {
        return Pattern.compile("^" + Pattern.quote(value.trim()) + "$", Pattern.CASE_INSENSITIVE);
    }

}
