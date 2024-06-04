package myconext.remotecreation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import myconext.exceptions.UserNotFoundException;
import myconext.manage.Manage;
import myconext.model.IdentityProvider;
import myconext.model.StatusResponse;
import myconext.model.User;
import myconext.repository.UserRepository;
import myconext.security.RemoteUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static myconext.SwaggerOpenIdConfig.BASIC_AUTHENTICATION_SCHEME_NAME;


@RestController
@ConditionalOnProperty("feature.remote_creation_api")
@RequestMapping(value = {"/api/remote-creation"}, produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = BASIC_AUTHENTICATION_SCHEME_NAME)
public class RemoteCreationController {

    private static final Log LOG = LogFactory.getLog(RemoteCreationController.class);

    private final UserRepository userRepository;
    private final Manage manage;

    public RemoteCreationController(UserRepository userRepository, Manage manage) {
        this.userRepository = userRepository;
        this.manage = manage;
    }

    @GetMapping(value = {"/email-eduid-exists"})
    @PreAuthorize("hasRole('ROLE_remote-creation')")
    @Operation(summary = "Does an eduID exists",
            description = "Does an eduID exists with the email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":200}")})}),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":404}")})})})
    public ResponseEntity<StatusResponse> emailEduIDExists(@Parameter(hidden = true) @AuthenticationPrincipal RemoteUser remoteUser,
                                                           @RequestParam(value = "email") String email) {
        LOG.debug(String.format("email-eduid-exists by %s for %s", remoteUser.getUsername(), email));

        userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
        return ResponseEntity.ok(new StatusResponse(HttpStatus.OK.value()));
    }

    @GetMapping(value = {"/eduid-exists"})
    @PreAuthorize("hasRole('ROLE_remote-creation')")
    @Operation(summary = "Does an eduID exists",
            description = "Does an eduID exists with the eduID value",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":200}")})}),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":404}")})})})
    public ResponseEntity<StatusResponse> remoteCreation(@Parameter(hidden = true) @AuthenticationPrincipal RemoteUser remoteUser,
                                                         @RequestParam(value = "eduID") String eduID) {
        LOG.debug(String.format("eduid-exists by %s for %s", remoteUser.getUsername(), eduID));

        userRepository.findByEduIDS_value(eduID).orElseThrow(() -> new UserNotFoundException("User not found"));
        return ResponseEntity.ok(new StatusResponse(HttpStatus.OK.value()));
    }

    @PostMapping(value = {"/eduid-institution-pseudonym"})
    @PreAuthorize("hasRole('ROLE_remote-creation')")
    @Operation(summary = "Return a eduID pseudonym for an institution",
            description = "Return a eduID pseudonym for an institution identified by the BRIN code",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = {@Content(schema = @Schema(implementation = EduIDValue.class),
                                    examples = {@ExampleObject(value = "{\"value\":fc75dcc7-6def-4054-b8ba-3c3cc504dd4b}")})}),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":404}")})})})
    public ResponseEntity<EduIDValue> eduIDForInstitution(@Parameter(hidden = true) @AuthenticationPrincipal RemoteUser remoteUser,
                                                          @RequestBody EduIDInstitutionPseudonym eduIDInstitutionPseudonym) {
        LOG.debug(String.format("eduid-institution-pseudonym by %s for %s", remoteUser.getUsername(), eduIDInstitutionPseudonym));

        User user = userRepository.findByEduIDS_value(eduIDInstitutionPseudonym.getEduID())
                .orElseThrow(() -> new UserNotFoundException(String.format("User with eduID %s not found", eduIDInstitutionPseudonym.getEduID())));
        IdentityProvider identityProvider = manage.findIdentityProviderByBrinCode(eduIDInstitutionPseudonym.getBrinCode())
                .orElseThrow(() -> new UserNotFoundException(String.format("IdentityProvider with BRIN code %s not found", eduIDInstitutionPseudonym.getBrinCode())));

        String eduIDValue = user.computeEduIdForIdentityProviderProviderIfAbsent(identityProvider, manage);
        userRepository.save(user);

        return ResponseEntity.ok(new EduIDValue(eduIDValue));
    }
}
