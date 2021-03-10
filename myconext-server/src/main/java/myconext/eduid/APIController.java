package myconext.eduid;

import myconext.exceptions.UserNotFoundException;
import myconext.model.LinkedAccount;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/myconext/api/eduid", produces = MediaType.APPLICATION_JSON_VALUE)
public class APIController {

    private final UserRepository userRepository;

    public APIController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/eppn")
    public Map<String, List<String>> eppn(BearerTokenAuthentication authentication, @RequestParam(value = "schachome", required = false) String schachome) {
        List<String> uids = (ArrayList<String>) authentication.getTokenAttributes().get("uids");
        User user = userRepository.findUserByUid(uids.get(0)).orElseThrow(UserNotFoundException::new);
        List<LinkedAccount> linkedAccounts = user.getLinkedAccounts();
        List<String> eppnList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(linkedAccounts)) {
            if (StringUtils.hasText(schachome)) {
                linkedAccounts.stream().filter(account -> schachome.equals(account.getSchacHomeOrganization()))
                        .findFirst()
                        .map(LinkedAccount::getEduPersonPrincipalName)
                        .ifPresent(eppnList::add);
            } else {
                eppnList = linkedAccounts.stream().map(linkedAccount -> linkedAccount.getEduPersonPrincipalName()).collect(Collectors.toList());
            }
        }
        Map<String, List<String>> result = new HashMap<>();
        result.put("eppn", eppnList);
        return result;
    }

}
