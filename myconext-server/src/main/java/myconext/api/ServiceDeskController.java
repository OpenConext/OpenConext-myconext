package myconext.api;

import lombok.Getter;
import myconext.model.ControlCode;
import myconext.model.User;
import myconext.repository.UserRepository;
import myconext.security.UserAuthentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/myconext/api/servicedesk"})
public class ServiceDeskController implements UserAuthentication {

    private static final Log LOG = LogFactory.getLog(ServiceDeskController.class);

    @Getter
    private final UserRepository userRepository;

    public ServiceDeskController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping("")
    public ResponseEntity<ControlCode> convertUserControlCode(Authentication authentication,
                                                              @RequestBody ControlCode controlCode) {
        User serviceDeskMember = userFromAuthentication(authentication);
        return ResponseEntity.ok(controlCode);
    }

}
