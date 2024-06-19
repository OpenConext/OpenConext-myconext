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
import myconext.exceptions.IdentityProviderNotFoundException;
import myconext.exceptions.UserNotFoundException;
import myconext.mail.MailBox;
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

import java.util.Optional;
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
    private final MailBox mailBox;
    private final AttributeMapper attributeMapper;

    public RemoteCreationController(UserRepository userRepository, Manage manage, MailBox mailBox, AttributeMapper attributeMapper) {
        this.userRepository = userRepository;
        this.manage = manage;
        this.mailBox = mailBox;
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
                                    examples = {@ExampleObject(value = "{\n" +
                                            "  \"timestamp\": 1717671062532,\n" +
                                            "  \"status\": 404,\n" +
                                            "  \"error\": \"Not Found\",\n" +
                                            "  \"exception\": \"myconext.exceptions.UserNotFoundException\",\n" +
                                            "  \"message\": \"User not found by email unknown@example.com\",\n" +
                                            "  \"path\": \"/api/remote-creation/email-eduid-exists\"\n" +
                                            "}")})})})
    public ResponseEntity<StatusResponse> emailEduIDExists(@Parameter(hidden = true) @AuthenticationPrincipal(errorOnInvalidType = true) RemoteUser remoteUser,
                                                           @RequestParam(value = "email") String email) {
        LOG.info(String.format("GET email-eduid-exists by %s for %s", remoteUser.getUsername(), email));

        userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found by email %s", email)));
        return ResponseEntity.ok(new StatusResponse(HttpStatus.OK.value()));
    }

    @GetMapping(value = {"/eduid-exists"})
    @PreAuthorize("hasRole('ROLE_remote-creation')")
    @Operation(summary = "Does an eduID exists",
            description = "Does an eduID account exists with the eduID identifier",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":200}")})}),
                    @ApiResponse(responseCode = "400", description = "BadRequest",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":400}")})}),
                    @ApiResponse(responseCode = "404", description = "Not found - eduID not found",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\n" +
                                            "  \"timestamp\": 1717671189426,\n" +
                                            "  \"status\": 404,\n" +
                                            "  \"error\": \"Not Found\",\n" +
                                            "  \"exception\": \"myconext.exceptions.UserNotFoundException\",\n" +
                                            "  \"message\": \"User not found by eduID 12345\",\n" +
                                            "  \"path\": \"/api/remote-creation/eduid-exists\"\n" +
                                            "}")})})})
    public ResponseEntity<StatusResponse> remoteCreation(@Parameter(hidden = true) @AuthenticationPrincipal(errorOnInvalidType = true) RemoteUser remoteUser,
                                                         @RequestParam(value = "eduID") String eduID) {
        LOG.info(String.format("GET eduid-exists by %s for %s", remoteUser.getUsername(), eduID));
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
                                    examples = {@ExampleObject(value = "{\n" +
                                            "  \"timestamp\": 1717671525908,\n" +
                                            "  \"status\": 404,\n" +
                                            "  \"error\": \"Not Found\",\n" +
                                            "  \"exception\": \"myconext.exceptions.UserNotFoundException\",\n" +
                                            "  \"message\": \"IdentityProvider with BRIN code AB!@ not found\",\n" +
                                            "  \"path\": \"/api/remote-creation/eduid-institution-pseudonym\"\n" +
                                            "}")})})})
    public ResponseEntity<EduIDValue> eduIDForInstitution(@Parameter(hidden = true) @AuthenticationPrincipal(errorOnInvalidType = true) RemoteUser remoteUser,
                                                          @RequestBody @Validated EduIDInstitutionPseudonym eduIDInstitutionPseudonym) {
        LOG.info(String.format("eduid-institution-pseudonym by %s for %s", remoteUser.getUsername(), eduIDInstitutionPseudonym));

        User user = this.findUserByEduIDValue(eduIDInstitutionPseudonym.getEduID())
                .orElseThrow(() -> new UserNotFoundException(String.format("User with eduID %s not found", eduIDInstitutionPseudonym.getEduID())));
        IdentityProvider identityProvider = manage.findIdentityProviderByBrinCode(eduIDInstitutionPseudonym.getBrinCode())
                .orElseThrow(() -> new IdentityProviderNotFoundException(String.format("IdentityProvider with BRIN code %s not found", eduIDInstitutionPseudonym.getBrinCode())));

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
                            content = {@Content(schema = @Schema(implementation = NewExternalEduID.class))}),
                    @ApiResponse(responseCode = "400", description = "BadRequest",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":400}")})}),
                    @ApiResponse(responseCode = "409", description = "Conflict - email already exists",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\n" +
                                            "  \"timestamp\": 1717672263253,\n" +
                                            "  \"status\": 409,\n" +
                                            "  \"error\": \"Conflict\",\n" +
                                            "  \"exception\": \"myconext.exceptions.DuplicateUserEmailException\",\n" +
                                            "  \"message\": \"Email already in use\",\n" +
                                            "  \"path\": \"/api/remote-creation/eduid-create\"\n" +
                                            "}")})})})
    public ResponseEntity<UpdateExternalEduID> createEduID(@Parameter(hidden = true) @AuthenticationPrincipal(errorOnInvalidType = true) RemoteUser remoteUser,
                                                           @RequestBody @Validated NewExternalEduID externalEduID) {
        String email = externalEduID.getEmail();
        String apiUserName = remoteUser.getUsername();

        LOG.info(String.format("POST eduid-create by %s for %s", apiUserName, email));

        externalEduID.validate();

        userRepository.findUserByEmail(email).ifPresent(u -> {
            throw new DuplicateUserEmailException("There already exists a user with email " + email);
        });
        IdentityProvider identityProvider = getIdentityProvider(remoteUser, externalEduID, apiUserName);
        String lastNamePrefix = externalEduID.getLastNamePrefix();
        String lastName = StringUtils.hasText(lastNamePrefix) ? String.format("%s %s", lastNamePrefix, externalEduID.getLastName()) : externalEduID.getLastName();
        User user = new User(UUID.randomUUID().toString(), externalEduID.getEmail(), externalEduID.getChosenName(),
                externalEduID.getFirstName(), lastName, remoteUser.getSchacHome(), LocaleContextHolder.getLocale().getLanguage(),
                identityProvider, manage);
        //Otherwise another email is sent out when the user logs in
        user.setNewUser(false);

        String eduIDValue = user.getEduIDS().get(0).getValue();
        UpdateExternalEduID updateExternalEduID = new UpdateExternalEduID(externalEduID, eduIDValue);

        ExternalLinkedAccount externalLinkedAccount = attributeMapper.createExternalLinkedAccount(externalEduID, IdpScoping.valueOf(apiUserName));
        user.getExternalLinkedAccounts().add(externalLinkedAccount);

        userRepository.save(user);
        mailBox.sendAccountConfirmation(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(updateExternalEduID);
    }

    @PutMapping(value = {"/eduid-update"})
    @PreAuthorize("hasRole('ROLE_remote-creation')")
    @Operation(summary = "Update an eduID",
            description = "Update an eduID",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = {@Content(schema = @Schema(implementation = NewExternalEduID.class))}),
                    @ApiResponse(responseCode = "400", description = "BadRequest",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":400}")})}),
                    @ApiResponse(responseCode = "409", description = "Conflict - email already exists",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\n" +
                                            "  \"timestamp\": 1717672653898,\n" +
                                            "  \"status\": 404,\n" +
                                            "  \"error\": \"Not Found\",\n" +
                                            "  \"exception\": \"myconext.exceptions.UserNotFoundException\",\n" +
                                            "  \"message\": \"User not found by eduID 12345\",\n" +
                                            "  \"path\": \"/api/remote-creation/eduid-update\"\n" +
                                            "}")})})})
    public ResponseEntity<UpdateExternalEduID> updateEduID(@Parameter(hidden = true) @AuthenticationPrincipal(errorOnInvalidType = true) RemoteUser remoteUser,
                                                           @RequestBody @Validated UpdateExternalEduID externalEduID) {
        String remoteUserName = remoteUser.getUsername();
        String email = externalEduID.getEmail();
        String eduIDValue = externalEduID.getEduIDValue();

        LOG.info(String.format("PUT eduid-update by %s for %s or %s", remoteUserName, email, eduIDValue));

        /*
         * There must be an existing User for the eduIDValue, because the account was created earlier with the POST eduid-create.
         * If there was an existing User for the email then the user was redirected and login with the eduID account for this email. In this case
         * we create new eduID value for the remote API institutionGUID
         */
        User user = this.findUserByEduIDValue(eduIDValue)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found by eduID %s", eduIDValue, email)));
        user.updateWithExternalEduID(externalEduID);
            //Not all attributes can be changed with an update
        Optional<ExternalLinkedAccount> optionalExternalLinkedAccount = user.getExternalLinkedAccounts().stream()
                .filter(account -> IdpScoping.valueOf(remoteUserName).equals(account.getIdpScoping()))
                .findAny();
        optionalExternalLinkedAccount.ifPresentOrElse(externalLinkedAccount -> {
            //Not all external attributes can be changed
            externalLinkedAccount.setVerification(externalEduID.getVerification());
            externalLinkedAccount.setBrinCode(externalEduID.getBrinCode());
            externalLinkedAccount.setDateOfBirth(AttributeMapper.parseDate(externalEduID.getDateOfBirth()));
        }, () -> {
            //Ensure there is an external account for this remoteAPI user
            IdentityProvider identityProvider = getIdentityProvider(remoteUser, externalEduID, remoteUserName);
            String provisionedEduIDValue = user.computeEduIdForIdentityProviderProviderIfAbsent(identityProvider, manage);
            externalEduID.setEduIDValue(provisionedEduIDValue);
            boolean externalLinkedAccountMissing = user.getExternalLinkedAccounts().stream()
                    .noneMatch(account -> IdpScoping.valueOf(remoteUserName).equals(account.getIdpScoping()));
            if (externalLinkedAccountMissing) {
                ExternalLinkedAccount externalLinkedAccount = attributeMapper.createExternalLinkedAccount(externalEduID, IdpScoping.valueOf(remoteUserName));
                user.getExternalLinkedAccounts().add(externalLinkedAccount);
            }
        });

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(externalEduID);
    }

    private static IdentityProvider getIdentityProvider(RemoteUser remoteUser, NewExternalEduID externalEduID, String remoteUserName) {
        RemoteProvider remoteProvider = new RemoteProvider(
                null,
                remoteUserName,
                remoteUserName,
                remoteUser.getInstitutionGUID(),
                String.format("https://static.surfconext.nl/logos/org/%s.png", remoteUser.getInstitutionGUID()));

        IdentityProvider identityProvider = new IdentityProvider(remoteProvider, externalEduID.getBrinCode());
        return identityProvider;
    }

}
