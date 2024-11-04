package myconext.eduid;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import myconext.api.HasUserRepository;
import myconext.exceptions.UserNotFoundException;
import myconext.model.EduID;
import myconext.model.ExternalLinkedAccount;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/myconext/api/eduid", produces = MediaType.APPLICATION_JSON_VALUE)
@Hidden
public class APIController implements HasUserRepository {

    private static final Log LOG = LogFactory.getLog(APIController.class);
    @Getter
    private final UserRepository userRepository;

    public APIController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/eppn")
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> eppn(BearerTokenAuthentication authentication, @RequestParam(value = "schachome", required = false) String schachome) {
        String clientId = (String) authentication.getTokenAttributes().get("client_id");

        LOG.info(String.format("Endpoint '/eppn/ called by authentication %s", clientId));

        List<Map<String, String>> results = getUser(authentication).linkedAccountsSorted().stream()
                .map(linkedAccount -> {
                    Map<String, String> info = new HashMap<>();
                    info.put("eppn", linkedAccount.getEduPersonPrincipalName());
                    info.put("schac_home_organization", linkedAccount.getSchacHomeOrganization());
                    return info;
                })
                .filter(info -> !StringUtils.hasText(schachome) || schachome.equals(info.get("schac_home_organization")))
                .collect(Collectors.toList());

        LOG.info(String.format("Endpoint '/eppn/ results %s for authentication %s", results, clientId));

        return results;
    }

    @GetMapping("/eduid")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, String>> eduid(BearerTokenAuthentication authentication) {
        String clientId = (String) authentication.getTokenAttributes().get("client_id");

        LOG.info(String.format("Endpoint '/eduid/ called by authentication %s", clientId));

        //Need to be backward compatible
        Optional<User> optionalUser = userRepository
                .findByEduIDS_serviceProviderEntityId(clientId)
                .or(() -> userRepository.findByEduIDS_Services_EntityId(clientId));
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = optionalUser.get();
        List<String> eduIDs = user.getEduIDS().stream()
                .filter(eduID -> clientId.equals(eduID.getServiceProviderEntityId()) ||
                        eduID.getServices().stream().anyMatch(service -> clientId.equals(service.getEntityId())))
                .map(EduID::getValue).collect(Collectors.toList());
        Map<String, String> results = eduIDs.isEmpty() ? new HashMap<>() : Collections.singletonMap("eduid", eduIDs.get(0));

        LOG.info(String.format("Endpoint '/eduid/ results %s for authentication %s", results, clientId));

        return ResponseEntity.ok(results);
    }

    @GetMapping("/links")
    public List<Map<String, String>> links(BearerTokenAuthentication authentication) {
        String clientId = (String) authentication.getTokenAttributes().get("client_id");

        LOG.info(String.format("Endpoint '/links/ called by authentication %s", clientId));

        User user = getUser(authentication);
        List<Map<String, String>> results = new ArrayList<>(user.linkedAccountsSorted().stream()
                .map(linkedAccount -> {
                    Map<String, String> info = new HashMap<>();
                    info.put("eppn", linkedAccount.getEduPersonPrincipalName());
                    info.put("schac_home_organization", linkedAccount.getSchacHomeOrganization());
                    if (linkedAccount.areNamesValidated()) {
                        info.put("validated_name", String.format("%s %s", linkedAccount.getGivenName(), linkedAccount.getFamilyName()));
                    }
                    return info;
                })
                .toList());

        List<Map<String, String>> externalValidatedNames = user.getExternalLinkedAccounts().stream()
                .filter(ExternalLinkedAccount::areNamesValidated)
                .map(externalLinkedAccount -> Map.of("validated_name",
                        String.format("%s %s", externalLinkedAccount.getGivenName(), externalLinkedAccount.getFamilyName())))
                .toList();
        results.addAll(externalValidatedNames);

        LOG.info(String.format("Endpoint '/links/ results %s for authentication %s", results, clientId));

        return results;
    }

    @SuppressWarnings("unchecked")
    private User getUser(BearerTokenAuthentication authentication) {
        List<String> uids = (ArrayList<String>) authentication.getTokenAttributes().get("uids");
        User user;
        if (CollectionUtils.isEmpty(uids)) {
            String eduid = (String) authentication.getTokenAttributes().get("eduid");
            LOG.info("EPPN API call: finding user by eduid: " + eduid);
            Optional<User> optionalUser = this.findUserByEduIDValue(eduid);
            user =  optionalUser.orElseThrow(() -> new UserNotFoundException(eduid));
        } else {
            String uid = uids.get(0);
            LOG.info("EPPN API call: finding user by uid: " + uid);
            user = userRepository.findUserByUid(uid).orElseThrow(() -> new UserNotFoundException(uid));
        }
        return user;
    }

}
