package myconext.aa;

import myconext.model.ServiceProvider;
import myconext.model.User;
import myconext.repository.ServiceProviderRepository;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/myconext/api")
public class AttributeAggregatorController {

    private static final Log LOG = LogFactory.getLog(AttributeAggregatorController.class);

    private UserRepository userRepository;
    private ServiceProviderRepository serviceProviderRepository;

    public AttributeAggregatorController(UserRepository userRepository, ServiceProviderRepository serviceProviderRepository) {
        this.userRepository = userRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    @GetMapping(value = {"attribute-aggregation"})
    public ResponseEntity<User> aggregate(Authentication authentication,
                                          @RequestParam("sp_entity_id") String spEntityId,
                                          @RequestParam("eduperson_principal_name") String eduPersonPrincipalName) {
        LOG.info(String.format("Attribute aggregation for eduPersonPrincipalName %s and spEntityId %s requested by %s",
                eduPersonPrincipalName, spEntityId, authentication.getPrincipal()));

        ServiceProvider serviceProvider = serviceProviderRepository
                .findByEntityId(spEntityId)
                .orElseGet(() -> serviceProviderRepository.save(new ServiceProvider(spEntityId)));

        User user = findUser(eduPersonPrincipalName, spEntityId);
        user.computeEduIdForServiceProviderIfAbsent(serviceProvider.getEntityId());

        List<UserAttribute> userAttributes = new ArrayList<>();
       // userAttributes.add(new UserAttribute("urn:mace:dir:attribute-def:eduPersonTargetedID", user.getUid())
        //TODO need to return SAML attributes names, see test_attribute_aggregation.py
        return ResponseEntity.ok(user);
    }

    private User findUser(String eduPersonPrincipalName, String spEntityId) {
        return userRepository
                .findUserByEduPersonPrincipalName(eduPersonPrincipalName)
                .orElseGet(() -> {
                    String[] split = eduPersonPrincipalName.split("@");
                    return split.length == 2 ?
                            userRepository.findUserByUidAndSchacHomeOrganization(split[0], split[1])
                                    .orElse(newUser(eduPersonPrincipalName, spEntityId)) :
                            newUser(eduPersonPrincipalName, spEntityId);
                });
    }

    private User newUser(String eduPersonPrincipalName, String spEntityId) {
        return new User(UUID.randomUUID().toString(), eduPersonPrincipalName, spEntityId);
    }
}
