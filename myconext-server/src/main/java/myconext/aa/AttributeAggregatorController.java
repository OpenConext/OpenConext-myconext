package myconext.aa;

import myconext.exceptions.UserNotFoundException;
import myconext.manage.ServiceProviderResolver;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


@RestController
@RequestMapping("/myconext/api")
public class AttributeAggregatorController {

    private static final Log LOG = LogFactory.getLog(AttributeAggregatorController.class);

    private final UserRepository userRepository;
    private final ServiceProviderResolver serviceProviderResolver;

    public AttributeAggregatorController(UserRepository userRepository,
                                         MongoTemplate mongoTemplate,
                                         ServiceProviderResolver serviceProviderResolver) {
        this.userRepository = userRepository;
        this.serviceProviderResolver = serviceProviderResolver;
    }

    @GetMapping(value = {"attribute-aggregation"})
    @PreAuthorize("hasRole('ROLE_attribute-aggregation')")
    public ResponseEntity<List<UserAttribute>> aggregate(@RequestParam("sp_entity_id") String spEntityId,
                                                         @RequestParam("eduperson_principal_name") String eduPersonPrincipalName) {
        Optional<User> userOptional = userRepository
                .findUserByLinkedAccounts_eduPersonPrincipalName(eduPersonPrincipalName);
        List<UserAttribute> userAttributes = new ArrayList<>();
        userOptional.ifPresent(user -> {
            String eduID = user.computeEduIdForServiceProviderIfAbsent(spEntityId, serviceProviderResolver);
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
        User user = userRepository.findByEduIDS_value(eduid).orElseThrow(() -> new UserNotFoundException(eduid));
        String eduId = user.computeEduIdForServiceProviderIfAbsent(spEntityId, serviceProviderResolver);
        userRepository.save(user);
        Map<String, String> result = new HashMap<>();
        result.put("eduid", eduId);
        if (StringUtils.hasText(spInstitutionGuid)) {
            user.getLinkedAccounts().stream()
                    .filter(linkedAccount -> linkedAccount.getInstitutionIdentifier().equals(spInstitutionGuid))
                    .findFirst()
                    .ifPresent(linkedAccount -> result.put("eduperson_principal_name", linkedAccount.getEduPersonPrincipalName()));
        }

        LOG.debug(String.format("Attribute manipulation response %s", result));

        return ResponseEntity.ok(result);
    }


}
