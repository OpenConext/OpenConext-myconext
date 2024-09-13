package myconext.invite;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.Getter;
import myconext.api.HasUserRepository;
import myconext.exceptions.IdentityProviderNotFoundException;
import myconext.exceptions.UserNotFoundException;
import myconext.manage.Manage;
import myconext.model.IdentityProvider;
import myconext.model.User;
import myconext.repository.UserRepository;
import myconext.security.RemoteUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static myconext.SwaggerOpenIdConfig.BASIC_AUTHENTICATION_SCHEME_NAME;


@RestController
@RequestMapping(value = {"/myconext/api/invite"}, produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = BASIC_AUTHENTICATION_SCHEME_NAME)
public class InviteController implements HasUserRepository {

    private static final Log LOG = LogFactory.getLog(InviteController.class);

    @Getter
    private final UserRepository userRepository;

    private final Manage manage;

    public InviteController(UserRepository userRepository,
                            Manage manage) {
        this.userRepository = userRepository;
        this.manage = manage;
    }

    @PostMapping(value = {"/provision-eduid"})
    @PreAuthorize("hasRole('ROLE_invite')")
    public ResponseEntity<EduIDProvision> provisionEduid(@Parameter(hidden = true) @AuthenticationPrincipal(errorOnInvalidType = true) RemoteUser remoteUser,
                                                           @RequestBody @Validated EduIDProvision eduIDProvision) {
        LOG.info(String.format("POST api/invite/provision-eduid by %s for %s", remoteUser.getUsername(), eduIDProvision));

        User user = userRepository.findByEduIDS_value(eduIDProvision.getEduIDValue())
                .orElseThrow(() -> new UserNotFoundException("User not found with eduID: " + eduIDProvision.getEduIDValue()));
        IdentityProvider identityProvider = manage.findIdentityProviderByInstitutionGUID(eduIDProvision.getInstitutionGUID())
                .orElseThrow(() -> new IdentityProviderNotFoundException("IdentityProvider not found with institutionGUID: " + eduIDProvision.getInstitutionGUID()));

        String newEduIDValue = user.computeEduIdForIdentityProviderProviderIfAbsent(identityProvider, manage);
        userRepository.save(user);
        eduIDProvision.setEduIDValue(newEduIDValue);
        return ResponseEntity.status(HttpStatus.CREATED).body(eduIDProvision);
    }
}
