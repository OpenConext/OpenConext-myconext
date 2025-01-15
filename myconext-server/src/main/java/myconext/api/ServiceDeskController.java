package myconext.api;

import lombok.Getter;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.model.ControlCode;
import myconext.model.ExternalLinkedAccount;
import myconext.model.User;
import myconext.repository.UserRepository;
import myconext.security.UserAuthentication;
import myconext.verify.AttributeMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;


@RestController
@RequestMapping(value = {"/myconext/api/servicedesk"})
public class ServiceDeskController implements UserAuthentication {

    private static final Log LOG = LogFactory.getLog(ServiceDeskController.class);

    @Getter
    private final UserRepository userRepository;
    private final AttributeMapper attributeMapper;

    public ServiceDeskController(UserRepository userRepository,
                                 AttributeMapper attributeMapper) {
        this.userRepository = userRepository;
        this.attributeMapper = attributeMapper;
    }

    @GetMapping("/user/{code}")
    public ResponseEntity<ControlCode> getUserControlCode(@PathVariable("code") String code) {
        LOG.debug("Fetching user with controlCode: " + code);

        User user = userRepository.findByControlCode_Code(code)
                .orElseThrow(() -> new UserNotFoundException(String.format("No user found with controlCode %s", code)));
        ControlCode controlCode = user.getControlCode();
        controlCode.setUserUid(user.getUid());

        return ResponseEntity.ok(controlCode);
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateDate(@RequestParam("dayOfBirth") String dayOfBirth) {
        LOG.debug("Validate date: " + dayOfBirth);

        Date date = AttributeMapper.parseDate(dayOfBirth);
        return ResponseEntity.ok(date != null);
    }

    @PutMapping("/approve")
    public ResponseEntity<Void> convertUserControlCode(Authentication authentication,
                                                       @RequestBody ControlCode controlCode) {
        String code = controlCode.getCode();
        User user = userRepository.findByControlCode_Code(code)
                .orElseThrow(() -> new UserNotFoundException(String.format("No user found with controlCode %s", code)));

        if (!user.getExternalLinkedAccounts().isEmpty() || !user.getLinkedAccounts().isEmpty()) {
            throw new ForbiddenException("User has already linked-accounts: " + user.getEmail());
        }
        if (!user.getUid().equals(controlCode.getUserUid())) {
            throw new ForbiddenException("User UID's do not match");
        }

        User serviceDeskMember = userFromAuthentication(authentication);

        LOG.info(String.format("Adding external linked account for service desk for user %s by user %s",
                user.getEmail(), serviceDeskMember.getEmail()));

        ExternalLinkedAccount externalLinkedAccount = attributeMapper.createFromControlCode(controlCode);
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
