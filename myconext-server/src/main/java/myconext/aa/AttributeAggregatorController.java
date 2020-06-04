package myconext.aa;

import myconext.exceptions.UserNotFoundException;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

@RestController
@RequestMapping("/myconext/api")
public class AttributeAggregatorController {

    private static final Log LOG = LogFactory.getLog(AttributeAggregatorController.class);

    private UserRepository userRepository;

    public AttributeAggregatorController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping(value = {"attribute-aggregation"})
    @PreAuthorize("hasRole('ROLE_attribute-aggregation')")
    public ResponseEntity<List<UserAttribute>> aggregate(Authentication authentication,
                                                         @RequestParam("sp_entity_id") String spEntityId,
                                                         @RequestParam("eduperson_principal_name") String eduPersonPrincipalName) {
        LOG.info(String.format("Attribute aggregation for eduPersonPrincipalName %s and spEntityId %s requested by %s",
                eduPersonPrincipalName, spEntityId, authentication.getPrincipal()));

        Optional<User> userOptional = userRepository
                .findUserByLinkedAccounts_eduPersonPrincipalName(eduPersonPrincipalName);
        List<UserAttribute> userAttributes = new ArrayList<>();
        userOptional.ifPresent(user -> {
            Optional<String> optionalEduID = user.computeEduIdForServiceProviderIfAbsent(spEntityId);
            optionalEduID.ifPresent(eduID -> userAttributes.add(
                    new UserAttribute("urn:mace:eduid.nl:1.1", eduID)));
        });
        return ResponseEntity.ok(userAttributes);
    }

    //Note that the spEntityId is the same as the  OIDC client ID
    @GetMapping(value = "attribute-manipulation")
    @PreAuthorize("hasRole('ROLE_attribute-manipulation')")
    public ResponseEntity<Map> manipulate(Authentication authentication,
                                          @RequestParam("sp_entity_id") String spEntityId,
                                          @RequestParam("uid") String uid,
                                          @RequestParam(value = "sp_institution_guid", required = false) String spInstitutionGuid) {
        LOG.info(String.format("Attribute manipulation for uid %s and spEntityId %s requested by %s",
                uid, spEntityId, authentication.getPrincipal()));
        User user = userRepository.findUserByUid(uid).orElseThrow(UserNotFoundException::new);
        boolean needToSave = !user.getEduIdPerServiceProvider().containsKey(spEntityId);
        String eduId = user.computeEduIdForServiceProviderIfAbsent(spEntityId).get();
        if (needToSave) {
            userRepository.save(user);
        }
        Map<String, String> result = new HashMap<>();
        result.put("eduid", eduId);
        if (StringUtils.hasText(spInstitutionGuid) ) {
            user.getLinkedAccounts().stream()
                    .filter(linkedAccount -> linkedAccount.getInstitutionIdentifier().equals(spInstitutionGuid))
                    .findFirst()
                    .ifPresent(linkedAccount -> result.put("eduperson_principal_name", linkedAccount.getEduPersonPrincipalName()));
        }
        return ResponseEntity.ok(result);
    }


}
