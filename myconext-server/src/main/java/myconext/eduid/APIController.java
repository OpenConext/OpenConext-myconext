package myconext.eduid;

import myconext.exceptions.UserNotFoundException;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
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
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> eppn(BearerTokenAuthentication authentication, @RequestParam(value = "schachome", required = false) String schachome) {
        List<String> uids = (ArrayList<String>) authentication.getTokenAttributes().get("uids");
        User user = userRepository.findUserByUid(uids.get(0)).orElseThrow(UserNotFoundException::new);
        return user.getLinkedAccounts().stream()
                .map(linkedAccount -> {
                    Map<String, String> info = new HashMap<>();
                    info.put("eppn", linkedAccount.getEduPersonPrincipalName());
                    info.put("schac_home_organization", linkedAccount.getSchacHomeOrganization());
                    return info;
                })
                .filter(info -> !StringUtils.hasText(schachome) || schachome.equals(info.get("schac_home_organization")))
                .collect(Collectors.toList());
    }

}
