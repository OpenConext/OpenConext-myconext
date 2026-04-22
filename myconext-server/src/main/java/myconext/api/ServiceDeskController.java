package myconext.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.RemoteException;
import myconext.exceptions.UserNotFoundException;
import myconext.model.*;
import myconext.repository.ExternalUserRepository;
import myconext.repository.UserRepository;
import myconext.verify.AttributeMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping(value = {"/myconext/api/servicedesk"})
public class ServiceDeskController {

    private static final Log LOG = LogFactory.getLog(ServiceDeskController.class);

    @Getter
    private final UserRepository userRepository;
    private final ExternalUserRepository externalUserRepository;
    private final AttributeMapper attributeMapper;
    private final String spBaseUrl;

    public ServiceDeskController(UserRepository userRepository,
                                 ExternalUserRepository externalUserRepository,
                                 AttributeMapper attributeMapper,
                                 @Value("${sp_redirect_url}") String spBaseUrl) {
        this.userRepository = userRepository;
        this.externalUserRepository = externalUserRepository;
        this.attributeMapper = attributeMapper;
        this.spBaseUrl = spBaseUrl;
    }

    @GetMapping("/me")
    public ResponseEntity<ExternalUser> me(Authentication authentication) {
        String userId = ((ExternalUser) authentication.getPrincipal()).getId();
        ExternalUser user = this.externalUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        return ResponseEntity.ok(user);
    }

    @GetMapping("/login")
    public View login(@RequestParam(value = "redirect_path", required = false) String redirectPath) {
        String target = spBaseUrl;
        if (StringUtils.hasText(redirectPath)) {
            String path = URLDecoder.decode(redirectPath, StandardCharsets.UTF_8);
            // Only allow internal, same-origin paths to avoid open-redirects
            if (path.startsWith("/") && !path.startsWith("//")) {
                target = spBaseUrl + path;
            }
        }
        LOG.debug(String.format("/login redirecting to %s", target));
        return new RedirectView(target, false);
    }

    @GetMapping("/logout")
    @Operation(summary = "Logout",
            description = "Logout the current logged in user")
    public ResponseEntity<StatusResponse> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new StatusResponse(HttpStatus.OK.value()));
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
    public ResponseEntity<ExternalLinkedAccount> convertUserControlCode(Authentication authentication,
                                                                        @RequestBody ControlCode controlCode) {
        try {
            return doConvertUserControlCode(authentication, controlCode);
        } catch (RuntimeException e) {
            ResponseStatus annotation = AnnotationUtils.getAnnotation(e.getClass(), ResponseStatus.class);
            HttpStatus status = annotation != null ? annotation.value() : HttpStatus.BAD_REQUEST;
            //This will log the reference to correlate to the user mail
            throw new RemoteException(status, e.getMessage(), e);
        }
    }

    private ResponseEntity<ExternalLinkedAccount> doConvertUserControlCode(Authentication authentication, ControlCode controlCode) throws RuntimeException {
        String code = controlCode.getCode();
        User user = userRepository.findByControlCode_Code(code)
                .orElseThrow(() -> new UserNotFoundException(String.format("No user found with controlCode %s", code)));

        if (!user.getExternalLinkedAccounts().isEmpty() || !user.getLinkedAccounts().isEmpty()) {
            throw new ForbiddenException("User has already linked-accounts: " + user.getEmail());
        }
        if (!user.getUid().equals(controlCode.getUserUid())) {
            throw new ForbiddenException("User UID's do not match");
        }

        String userUid = Optional.ofNullable(
                        ((OidcUser) authentication.getPrincipal()).getClaimAsStringList("uids"))
                .filter(l -> !l.isEmpty())
                .map(List::getFirst)
                .orElseThrow(() -> new ForbiddenException("Missing 'uids' claim"));

        ExternalUser serviceDeskMember = this.externalUserRepository.findUserByUid(userUid).orElseThrow(() -> new UserNotFoundException(userUid));

        LOG.info(String.format("Adding external linked account for service desk for user %s by user %s",
                user.getEmail(), serviceDeskMember.getEmail()));

        ExternalLinkedAccount externalLinkedAccount = attributeMapper.createFromControlCode(controlCode);
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        //The controlCode is now invalid as already used
        user.setControlCode(null);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(externalLinkedAccount);
    }

}
