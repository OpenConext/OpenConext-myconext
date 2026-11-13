package myconext.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import myconext.activity.ActivityContext;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.RemoteException;
import myconext.exceptions.UserNotFoundException;
import myconext.invite.Invite;
import myconext.manage.Manage;
import myconext.model.*;
import myconext.repository.ExternalUserRepository;
import myconext.repository.ServiceDeskUserRepository;
import myconext.repository.UserRepository;
import myconext.verify.AttributeMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping(value = {"/myconext/api/servicedesk"})
public class ServiceDeskController {

    private static final Log LOG = LogFactory.getLog(ServiceDeskController.class);

    @Getter
    private final UserRepository userRepository;
    private final ExternalUserRepository externalUserRepository;
    private final ServiceDeskUserRepository serviceDeskUserRepository;
    private final AttributeMapper attributeMapper;
    private final Manage manage;
    private final Invite invite;

    public ServiceDeskController(UserRepository userRepository,
                                 ExternalUserRepository externalUserRepository,
                                 ServiceDeskUserRepository serviceDeskUserRepository,
                                 AttributeMapper attributeMapper,
                                 Manage manage,
                                 Invite invite) {
        this.userRepository = userRepository;
        this.externalUserRepository = externalUserRepository;
        this.serviceDeskUserRepository = serviceDeskUserRepository;
        this.attributeMapper = attributeMapper;
        this.manage = manage;
        this.invite = invite;
    }

    @GetMapping("/me")
    public ResponseEntity<ExternalUser> me(Authentication authentication) {
        String userId = (String) ((OidcUser) authentication.getPrincipal()).getClaims().get("id");
        ExternalUser user = this.externalUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    @Operation(summary = "Search users",
            description = "Search the users with a linked account for the institution of the logged in service desk member")
    public ResponseEntity<ServiceDeskUserSearchResult> searchUsers(
            Authentication authentication,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "mfa", required = false) List<String> mfaMethods,
            @RequestParam(value = "blocked", required = false) Boolean blocked,
            @RequestParam(value = "sort", required = false, defaultValue = "name") String sort,
            @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        String userId = (String) ((OidcUser) authentication.getPrincipal()).getClaims().get("id");
        ExternalUser serviceDeskMember = this.externalUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String schacHomeOrganization = serviceDeskMember.getSchacHomeOrganization();
        ServiceDeskUserSearchResult result = serviceDeskUserRepository.search(schacHomeOrganization,
                query, mfaMethods, blocked, sort, !"desc".equalsIgnoreCase(direction), page, size);

        Map<String, List<String>> roles = rolesOfInstitution(schacHomeOrganization);
        if (roles.isEmpty()) {
            return ResponseEntity.ok(result);
        }
        List<ServiceDeskUser> users = result.users().stream()
                .map(user -> user.withRoles(roles.getOrDefault(user.eduPersonPrincipalName(), List.of())))
                .toList();
        return ResponseEntity.ok(new ServiceDeskUserSearchResult(users, result.total(), result.page(), result.pageSize()));
    }

    private Map<String, List<String>> rolesOfInstitution(String schacHomeOrganization) {
        //The roles in Invite are coupled to the organization GUID of the institution in Manage
        return manage.findIdentityProviderByDomainName(schacHomeOrganization)
                .map(IdentityProvider::getInstitutionGuid)
                .filter(StringUtils::hasText)
                .map(invite::rolesByEduPersonPrincipalName)
                .orElse(Map.of());
    }

    @GetMapping("/overview")
    @Operation(summary = "Overview",
            description = "The number of eduID accounts in time and the services they are logged into, for the institution of the logged in service desk member")
    public ResponseEntity<ServiceDeskOverview> overview(Authentication authentication) {
        String userId = (String) ((OidcUser) authentication.getPrincipal()).getClaims().get("id");
        ExternalUser serviceDeskMember = this.externalUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return ResponseEntity.ok(serviceDeskUserRepository.overview(serviceDeskMember.getSchacHomeOrganization()));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "User details",
            description = "All details of a user with a linked account for the institution of the logged in service desk member")
    public ResponseEntity<ServiceDeskUserDetail> userDetail(Authentication authentication,
                                                            @PathVariable("id") String id) {
        String userId = (String) ((OidcUser) authentication.getPrincipal()).getClaims().get("id");
        ExternalUser serviceDeskMember = this.externalUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        //Users of other institutions are not found
        return ResponseEntity.ok(serviceDeskUserRepository.findDetail(id, serviceDeskMember.getSchacHomeOrganization())
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @PutMapping("/users/{id}/block")
    @Operation(summary = "Block user",
            description = "Block a user with a linked account for the institution of the logged in service desk member")
    public ResponseEntity<StatusResponse> blockUser(Authentication authentication,
                                                    @PathVariable("id") String id) {
        String userId = (String) ((OidcUser) authentication.getPrincipal()).getClaims().get("id");
        ExternalUser serviceDeskMember = this.externalUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        //Users of other institutions are not found
        if (!serviceDeskUserRepository.block(id, serviceDeskMember.getSchacHomeOrganization(),
                ActivityContext.actor(serviceDeskMember.getId(), serviceDeskMember.getName()))) {
            throw new UserNotFoundException(id);
        }
        LOG.info(String.format("Service desk member %s blocked user %s", serviceDeskMember.getEmail(), id));
        return ResponseEntity.ok(new StatusResponse(HttpStatus.OK.value()));
    }

    @PutMapping("/users/{id}/unblock")
    @Operation(summary = "Unblock user",
            description = "Unblock a user with a linked account for the institution of the logged in service desk member")
    public ResponseEntity<StatusResponse> unblockUser(Authentication authentication,
                                                      @PathVariable("id") String id) {
        String userId = (String) ((OidcUser) authentication.getPrincipal()).getClaims().get("id");
        ExternalUser serviceDeskMember = this.externalUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        //Users of other institutions are not found
        if (!serviceDeskUserRepository.unblock(id, serviceDeskMember.getSchacHomeOrganization(),
                ActivityContext.actor(serviceDeskMember.getId(), serviceDeskMember.getName()))) {
            throw new UserNotFoundException(id);
        }
        LOG.info(String.format("Service desk member %s unblocked user %s", serviceDeskMember.getEmail(), id));
        return ResponseEntity.ok(new StatusResponse(HttpStatus.OK.value()));
    }

    @PutMapping("/users/{id}/reset-mfa")
    @Operation(summary = "Reset MFA",
            description = "Remove the MFA methods of a user with a linked account for the institution of the logged in service desk member")
    public ResponseEntity<StatusResponse> resetMfa(Authentication authentication,
                                                   @PathVariable("id") String id) {
        String userId = (String) ((OidcUser) authentication.getPrincipal()).getClaims().get("id");
        ExternalUser serviceDeskMember = this.externalUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        //Users of other institutions are not found
        if (!serviceDeskUserRepository.resetMfa(id, serviceDeskMember.getSchacHomeOrganization(),
                ActivityContext.actor(serviceDeskMember.getId(), serviceDeskMember.getName()))) {
            throw new UserNotFoundException(id);
        }
        LOG.info(String.format("Service desk member %s reset the MFA of user %s", serviceDeskMember.getEmail(), id));
        return ResponseEntity.ok(new StatusResponse(HttpStatus.OK.value()));
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
        externalLinkedAccount.setSchacHomeOrganization(serviceDeskMember.getSchacHomeOrganization());
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        //The controlCode is now invalid as already used
        user.setControlCode(null);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(externalLinkedAccount);
    }

}
