package myconext.remotecreation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.Getter;
import myconext.api.HasUserRepository;
import myconext.exceptions.DuplicateUserEmailException;
import myconext.exceptions.ResourceGoneException;
import myconext.exceptions.UserNotFoundException;
import myconext.manage.Manage;
import myconext.model.*;
import myconext.repository.UserRepository;
import myconext.security.RemoteUser;
import myconext.verify.AttributeMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static myconext.SwaggerOpenIdConfig.BASIC_AUTHENTICATION_SCHEME_NAME;


@RestController
@ConditionalOnProperty("feature.remote_creation_api")
@RequestMapping(value = {"/api/remote-creation"}, produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = BASIC_AUTHENTICATION_SCHEME_NAME)
public class RemoteCreationController implements HasUserRepository {

    private static final Log LOG = LogFactory.getLog(RemoteCreationController.class);
    @Getter
    private final UserRepository userRepository;
    private final Manage manage;
    private final AttributeMapper attributeMapper;

    public RemoteCreationController(UserRepository userRepository, Manage manage, AttributeMapper attributeMapper) {
        this.userRepository = userRepository;
        this.manage = manage;
        this.attributeMapper = attributeMapper;
    }

    @GetMapping(value = {"/email-eduid-exists"})
    @PreAuthorize("hasRole('ROLE_remote-creation')")
    @Operation(summary = "Does an eduID exists",
            description = "Does an eduID exists with the email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":200}")})}),
                    @ApiResponse(responseCode = "400", description = "BadRequest",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":400}")})}),
                    @ApiResponse(responseCode = "404", description = "Not found - email not found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":404}")})})})
    public ResponseEntity<StatusResponse> emailEduIDExists(@Parameter(hidden = true) @AuthenticationPrincipal RemoteUser remoteUser,
                                                           @RequestParam(value = "email") String email) {
        LOG.debug(String.format("GET email-eduid-exists by %s for %s", remoteUser.getUsername(), email));

        userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found by email %s", email)));
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
                    @ApiResponse(responseCode = "400", description = "BadRequest",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":400}")})}),
                    @ApiResponse(responseCode = "404", description = "Not found - eduID not found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":404}")})})})
    public ResponseEntity<StatusResponse> remoteCreation(@Parameter(hidden = true) @AuthenticationPrincipal RemoteUser remoteUser,
                                                         @RequestParam(value = "eduID") String eduID) {
        LOG.debug(String.format("GET eduid-exists by %s for %s", remoteUser.getUsername(), eduID));
        this.findUserByEduIDValue(eduID)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found by eduID %s", eduID)));
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
                    @ApiResponse(responseCode = "400", description = "BadRequest",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":400}")})}),
                    @ApiResponse(responseCode = "404", description = "Not found - eduID or BRIN code not found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":404}")})})})
    public ResponseEntity<EduIDValue> eduIDForInstitution(@Parameter(hidden = true) @AuthenticationPrincipal RemoteUser remoteUser,
                                                          @RequestBody @Validated EduIDInstitutionPseudonym eduIDInstitutionPseudonym) {
        LOG.debug(String.format("eduid-institution-pseudonym by %s for %s", remoteUser.getUsername(), eduIDInstitutionPseudonym));

        User user = this.findUserByEduIDValue(eduIDInstitutionPseudonym.getEduID())
                .orElseThrow(() -> new UserNotFoundException(String.format("User with eduID %s not found", eduIDInstitutionPseudonym.getEduID())));
        IdentityProvider identityProvider = manage.findIdentityProviderByBrinCode(eduIDInstitutionPseudonym.getBrinCode())
                .orElseThrow(() -> new UserNotFoundException(String.format("IdentityProvider with BRIN code %s not found", eduIDInstitutionPseudonym.getBrinCode())));

        String eduIDValue = user.computeEduIdForIdentityProviderProviderIfAbsent(identityProvider, manage);
        userRepository.save(user);

        return ResponseEntity.ok(new EduIDValue(eduIDValue));
    }

    @PostMapping(value = {"/eduid-create"})
    @PreAuthorize("hasRole('ROLE_remote-creation')")
    @Operation(summary = "Create an eduID",
            description = "Create an eduID",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = {@Content(schema = @Schema(implementation = ExternalEduID.class))}),
                    @ApiResponse(responseCode = "400", description = "BadRequest",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":400}")})}),
                    @ApiResponse(responseCode = "409", description = "Conflict - email already exists",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":409}")})})})
    public ResponseEntity<ExternalEduID> createEduID(@Parameter(hidden = true) @AuthenticationPrincipal RemoteUser remoteUser,
                                                     @RequestBody @Validated ExternalEduID externalEduID) {
        String email = externalEduID.getEmail();
        String apiUserName = remoteUser.getUsername();

        LOG.debug(String.format("POST eduid-create by %s for %s", apiUserName, email));

        externalEduID.validate();

        userRepository.findUserByEmail(email).ifPresent(u -> {
            throw new DuplicateUserEmailException();
        });
        IdentityProvider identityProvider = new IdentityProvider(null, externalEduID.getBrinCode(), remoteUser.getInstitutionGUID(),
                apiUserName, apiUserName,
                String.format("https://static.surfconext.nl/logos/org/%s.png", remoteUser.getInstitutionGUID()));
        User user = new User(UUID.randomUUID().toString(), externalEduID.getEmail(), externalEduID.getChosenName(),
                externalEduID.getFirstName(), externalEduID.getLastName(), remoteUser.getSchacHome(), LocaleContextHolder.getLocale().getLanguage(),
                identityProvider, manage);

        String eduIDValue = user.getEduIDS().get(0).getValue();
        externalEduID.setEduIDValue(eduIDValue);
        ExternalLinkedAccount externalLinkedAccount = attributeMapper.createExternalLinkedAccount(externalEduID, IdpScoping.valueOf(apiUserName));
        user.getExternalLinkedAccounts().add(externalLinkedAccount);

        userRepository.save(user);

        return ResponseEntity.ok(externalEduID);
    }

    @PutMapping(value = {"/eduid-update"})
    @PreAuthorize("hasRole('ROLE_remote-creation')")
    @Operation(summary = "Update an eduID",
            description = "Update an eduID",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = {@Content(schema = @Schema(implementation = ExternalEduID.class))}),
                    @ApiResponse(responseCode = "400", description = "BadRequest",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":400}")})})})
    public ResponseEntity<ExternalEduID> updateEduID(@Parameter(hidden = true) @AuthenticationPrincipal RemoteUser remoteUser,
                                                     @RequestBody @Validated ExternalEduID externalEduID) {
        String email = externalEduID.getEmail();
        String eduIDValue = externalEduID.getEduIDValue();

        LOG.debug(String.format("PUT eduid-update by %s for %s or %s", remoteUser.getUsername(), email, eduIDValue));

        /*
         * Either there is an existing User for the eduIDValue in case the account was created earlier with the POST eduid-create,
         * or there is an existing User for the email as there was already an eduID account for this email. In this case
         * we create new eduID value for the remote API institutionGUID
         */
        User user = this.findUserByEduIDValue(eduIDValue)
                .or(() -> userRepository.findUserByEmail(email))
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found by eduID %s or email %s", eduIDValue, email)));
        if (StringUtils.hasText(eduIDValue)) {
            //Not all attributes can be changed with an update
            user.setGivenName(externalEduID.getFirstName());
            user.setFamilyName(externalEduID.getLastName());
            ExternalLinkedAccount externalLinkedAccount = user.getExternalLinkedAccounts().stream()
                    .filter(account -> IdpScoping.valueOf(remoteUser.getUsername()).equals(account.getIdpScoping()))
                    .findAny()
                    .orElseThrow(() -> new ResourceGoneException(String.format("User %s has removed the studie link link", user.getEmail())));
            //Not all external attributes can be changed
            externalLinkedAccount.setVerification(externalEduID.getVerification());
            externalLinkedAccount.setBrinCode(externalEduID.getBrinCode());
        } else {
            //Ensure there is an external account for this remoteAPI user
            String apiUserName = remoteUser.getUsername();
            IdentityProvider identityProvider = new IdentityProvider(null, externalEduID.getBrinCode(), remoteUser.getInstitutionGUID(),
                    apiUserName, apiUserName,
                    String.format("https://static.surfconext.nl/logos/org/%s.png", remoteUser.getInstitutionGUID()));
            String provisionedEduIDValue = user.computeEduIdForIdentityProviderProviderIfAbsent(identityProvider, manage);
            externalEduID.setEduIDValue(provisionedEduIDValue);
            boolean externalLinkedAccountMissing = user.getExternalLinkedAccounts().stream()
                    .noneMatch(account -> IdpScoping.valueOf(remoteUser.getUsername()).equals(account.getIdpScoping()));
            if (externalLinkedAccountMissing) {
                ExternalLinkedAccount externalLinkedAccount = attributeMapper.createExternalLinkedAccount(externalEduID, IdpScoping.valueOf(apiUserName));
                user.getExternalLinkedAccounts().add(externalLinkedAccount);
            }
        }

        userRepository.save(user);

        return ResponseEntity.ok(externalEduID);
    }

}
