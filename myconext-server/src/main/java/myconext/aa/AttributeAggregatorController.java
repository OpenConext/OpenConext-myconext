package myconext.aa;

import io.swagger.v3.oas.annotations.Hidden;
import myconext.manage.ServiceProviderResolver;
import myconext.model.EduID;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/myconext/api")
@Hidden
public class AttributeAggregatorController {

    private static final Log LOG = LogFactory.getLog(AttributeAggregatorController.class);

    private final UserRepository userRepository;
    private final ServiceProviderResolver serviceProviderResolver;
    private final String schacHomeOrganization;

    public AttributeAggregatorController(UserRepository userRepository,
                                         ServiceProviderResolver serviceProviderResolver,
                                         @Value("${schac_home_organization}") String schacHomeOrganization) {
        this.userRepository = userRepository;
        this.serviceProviderResolver = serviceProviderResolver;
        this.schacHomeOrganization = schacHomeOrganization;
    }

    @GetMapping(value = {"attribute-aggregation"})
    @PreAuthorize("hasRole('ROLE_attribute-aggregation')")
    public ResponseEntity<List<UserAttribute>> aggregate(Authentication authentication,
                                                         @RequestParam("sp_entity_id") String spEntityId,
                                                         @RequestParam("eduperson_principal_name") String eduPersonPrincipalName) {
        LOG.debug(String.format("Attribute aggregation request %s %s", spEntityId, eduPersonPrincipalName));
        Optional<User> userOptional = Optional.empty();
        //it might be that the eppn if from the eduID IdP, and then we can look up the user based on that
        int indexOfAt = eduPersonPrincipalName.indexOf("@");
        String schacHome = eduPersonPrincipalName.substring(indexOfAt + 1);
        if (this.schacHomeOrganization.equals(schacHome)) {
            String uid = eduPersonPrincipalName.substring(0, indexOfAt);
            userOptional = userRepository.findUserByUid(uid);
        } else {
            userOptional = userRepository
                    .findUserByLinkedAccounts_eduPersonPrincipalName(eduPersonPrincipalName);
        }
        List<UserAttribute> userAttributes = new ArrayList<>();
        userOptional.ifPresent(user -> {
            String eduID = user.computeEduIdForServiceProviderIfAbsent(spEntityId, serviceProviderResolver);
            userRepository.save(user);
            userAttributes.add(new UserAttribute("urn:mace:eduid.nl:1.1", eduID));
        });

        LOG.debug(String.format("Attribute aggregation response %s", userAttributes));

        return ResponseEntity.ok(userAttributes);
    }

    //Note that the spEntityId is the same as the  OIDC client ID
    @GetMapping(value = "attribute-manipulation")
    @PreAuthorize("hasRole('ROLE_attribute-manipulation')")
    public ResponseEntity<Map> manipulate(@RequestParam("sp_entity_id") String spEntityId,
                                          @RequestParam("eduid") String eduid,
                                          @RequestParam(value = "sp_institution_guid", required = false) String spInstitutionGuid) {
        Optional<User> userOptional = userRepository.findByEduIDS_value(eduid);
        if (!userOptional.isPresent()) {
            LOG.warn(String.format("Attribute manipulation request for %s with an eduID %s that is not present", spEntityId, eduid));
            return ResponseEntity.ok(new HashMap<>());
        }
        User user = userOptional.get();
        String eduId = user.computeEduIdForServiceProviderIfAbsent(spEntityId, serviceProviderResolver);
        userRepository.save(user);
        Map<String, String> result = new HashMap<>();
        result.put("eduid", eduId);
        if (StringUtils.hasText(spInstitutionGuid)) {
            user.getLinkedAccounts().stream()
                    .filter(linkedAccount -> linkedAccount.getInstitutionIdentifier().equals(spInstitutionGuid))
                    .findFirst()
                    .ifPresent(linkedAccount -> {
                        result.put("eduperson_principal_name", linkedAccount.getEduPersonPrincipalName());
                        if (StringUtils.hasText(linkedAccount.getSubjectId())) {
                            result.put("subject_id", linkedAccount.getSubjectId());
                        }

                    });
        }

        LOG.debug(String.format("Attribute manipulation response %s", result));

        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "system/eduid-duplicates")
    @PreAuthorize("hasRole('ROLE_system')")
    public ResponseEntity<Map<String, List<EduID>>> eduIdDuplicates() {
        List<EduID> eduIDs = userRepository.findAll().stream()
                .map(User::getEduIDS).flatMap(Collection::stream)
                .collect(Collectors.toList()).stream()
                .filter(eduID -> StringUtils.hasText(eduID.getServiceInstutionGuid()))
                .collect(Collectors.toList());
        Set<EduID> uniqueSet = new TreeSet<>(Comparator.comparing(EduID::getServiceProviderEntityId));
        uniqueSet.addAll(eduIDs);
        Map<String, List<EduID>> eduIdValuesGroupedBy = uniqueSet.stream()
                .collect(Collectors.groupingBy(EduID::getServiceInstutionGuid));
        eduIdValuesGroupedBy.values().removeIf(l -> l.size() < 2);
        return ResponseEntity.ok(eduIdValuesGroupedBy);
    }

}
