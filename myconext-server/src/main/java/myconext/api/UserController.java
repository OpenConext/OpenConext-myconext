package myconext.api;

import myconext.exceptions.DuplicateUserEmailException;
import myconext.exceptions.ExpiredAuthenticationException;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.mail.MailBox;
import myconext.manage.ServiceNameResolver;
import myconext.model.MagicLinkRequest;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.UpdateUserSecurityRequest;
import myconext.model.User;
import myconext.model.UserResponse;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.UserRepository;
import myconext.security.EmailGuessingPrevention;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/myconext/api")
public class UserController {

    private static final Log LOG = LogFactory.getLog(UserController.class);

    private UserRepository userRepository;
    private AuthenticationRequestRepository authenticationRequestRepository;
    private MailBox mailBox;
    private ServiceNameResolver serviceNameResolver;
    private String magicLinkUrl;
    private String schacHomeOrganization;
    private String guestIdpEntityId;

    private SecureRandom random = new SecureRandom();
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(-1, random);
    private EmailGuessingPrevention emailGuessingPreventor;

    public UserController(UserRepository userRepository,
                          AuthenticationRequestRepository authenticationRequestRepository,
                          MailBox mailBox,
                          ServiceNameResolver serviceNameResolver,
                          @Value("${email.magic-link-url}") String magicLinkUrl,
                          @Value("${schac_home_organization}") String schacHomeOrganization,
                          @Value("${guest_idp_entity_id}") String guestIdpEntityId,
                          @Value("${email_guessing_sleep_millis}") int emailGuessingSleepMillis) {
        this.userRepository = userRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.mailBox = mailBox;
        this.serviceNameResolver = serviceNameResolver;
        this.magicLinkUrl = magicLinkUrl;
        this.schacHomeOrganization = schacHomeOrganization;
        this.guestIdpEntityId = guestIdpEntityId;
        this.emailGuessingPreventor = new EmailGuessingPrevention(emailGuessingSleepMillis);
    }

    @PostMapping("/idp/magic_link_request")
    public ResponseEntity newMagicLinkRequest(@Valid @RequestBody MagicLinkRequest magicLinkRequest) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);

        User user = magicLinkRequest.getUser();

        emailGuessingPreventor.potentialUserEmailGuess();

        userRepository.findUserByEmail(user.getEmail()).ifPresent(u -> {
            throw new DuplicateUserEmailException();
        });

        //prevent not-wanted attributes in the database
        User userToSave = new User(UUID.randomUUID().toString(), user.getEmail(), user.getGivenName(),
                user.getFamilyName(), schacHomeOrganization, guestIdpEntityId);
        userToSave.encryptPassword(user.getPassword(), passwordEncoder);
        userToSave = userRepository.save(userToSave);

        return this.doMagicLink(userToSave, samlAuthenticationRequest, magicLinkRequest, false);
    }

    @PutMapping("/idp/magic_link_request")
    public ResponseEntity magicLinkRequest(@Valid @RequestBody MagicLinkRequest magicLinkRequest) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);

        User providedUser = magicLinkRequest.getUser();

        emailGuessingPreventor.potentialUserEmailGuess();

        Optional<User> optionalUser = userRepository.findUserByEmail(providedUser.getEmail());
        User user = optionalUser.orElseThrow(UserNotFoundException::new);
        if (magicLinkRequest.isUsePassword()) {
            if (!passwordEncoder.matches(providedUser.getPassword(), user.getPassword())) {
                throw new ForbiddenException();
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                    user.getAuthorities());

            LOG.info(String.format("User %s successfully logged in with password", user.getUsername()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        return doMagicLink(user, samlAuthenticationRequest, magicLinkRequest, magicLinkRequest.isUsePassword());
    }

    @GetMapping(value = {"/sp/me", "sp/migrate/merge", "sp/migrate/proceed"})
    public ResponseEntity me(Authentication authentication) {
        User user = userRepository.findOneUserByEmail(((User) authentication.getPrincipal()).getEmail());
        List<SamlAuthenticationRequest> samlAuthenticationRequests = authenticationRequestRepository.findByUserIdAndRememberMe(user.getId(), true);
        return ResponseEntity.ok(new UserResponse(user, !samlAuthenticationRequests.isEmpty()));
    }

    @DeleteMapping("/sp/forget")
    public ResponseEntity forgetMe(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String userId = user.getId();
        Long count = authenticationRequestRepository.deleteByUserId(userId);

        LOG.info(String.format("Do not remember user %s anymore", user.getUsername()));

        return ResponseEntity.ok(count);
    }

    @PutMapping("/sp/update")
    public ResponseEntity updateUserProfile(Authentication authentication, @RequestBody User deltaUser) {
        User user = verifyAndFetchUser(authentication, deltaUser);

        user.setFamilyName(deltaUser.getFamilyName());
        user.setGivenName(deltaUser.getGivenName());
        user.validate();

        userRepository.save(user);

        LOG.info(String.format("Update user profile for %s", user.getUsername()));

        return ResponseEntity.status(201).body(new UserResponse(user, false));
    }

    @PutMapping("/sp/security")
    public ResponseEntity updateUserSecurity(Authentication authentication, @RequestBody UpdateUserSecurityRequest updateUserRequest) {
        User deltaUser = userRepository.findById(updateUserRequest.getUserId()).orElseThrow(UserNotFoundException::new);
        User user = verifyAndFetchUser(authentication, deltaUser);
        if (StringUtils.hasText(user.getPassword())) {
            if (!passwordEncoder.matches(updateUserRequest.getCurrentPassword(), user.getPassword())) {
                throw new ForbiddenException();
            }
        }
        user.encryptPassword(updateUserRequest.getNewPassword(), passwordEncoder);
        userRepository.save(user);

        LOG.info(String.format("Updates / set password for user %s", user.getUsername()));

        return ResponseEntity.status(201).body(new UserResponse(user, false));
    }

    @GetMapping("/sp/logout")
    public ResponseEntity logout(HttpServletRequest request) throws URISyntaxException {
        return doLogout(request);
    }

    @DeleteMapping("/sp/delete")
    public ResponseEntity deleteUser(Authentication authentication, HttpServletRequest request) throws URISyntaxException {
        User principal = (User) authentication.getPrincipal();
        userRepository.deleteById(principal.getId());

        LOG.info(String.format("Deleted user %s", principal.getEmail()));

        return doLogout(request);
    }

    private ResponseEntity doLogout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Collections.singletonMap("status", "oke"));
    }

    private User verifyAndFetchUser(Authentication authentication, User deltaUser) {
        return verifyAndFetchUser(authentication, deltaUser.getId());
    }

    private User verifyAndFetchUser(Authentication authentication, String id) {
        User principal = (User) authentication.getPrincipal();
        if (!principal.getId().equals(id)) {
            throw new ForbiddenException();
        }
        //Strictly not necessary, but mid-air collisions can occur in theory
        return userRepository.findOneUserByEmail(principal.getEmail());
    }

    private ResponseEntity doMagicLink(User user, SamlAuthenticationRequest samlAuthenticationRequest, MagicLinkRequest magicLinkRequest,
                                       boolean passwordFlow) {
        samlAuthenticationRequest.setHash(hash());
        samlAuthenticationRequest.setUserId(user.getId());
        samlAuthenticationRequest.setRememberMe(magicLinkRequest.isRememberMe());
        if (magicLinkRequest.isRememberMe()) {
            samlAuthenticationRequest.setRememberMeValue(UUID.randomUUID().toString());
        }

        authenticationRequestRepository.save(samlAuthenticationRequest);
        if (passwordFlow) {
            return ResponseEntity.status(201).body(Collections.singletonMap("url", this.magicLinkUrl + "?h=" + samlAuthenticationRequest.getHash()));
        } else {
            if (user.isNewUser()) {
                LOG.info(String.format("Sending magic link for new user %s", user.getUsername()));

                mailBox.sendAccountVerification(user, samlAuthenticationRequest.getHash());
            } else {
                String serviceName = serviceNameResolver.resolve(samlAuthenticationRequest.getRequesterEntityId());

                LOG.info(String.format("Sending magic link for existing user %s", user.getUsername()));

                mailBox.sendMagicLink(user, samlAuthenticationRequest.getHash(), serviceName);
            }
            return ResponseEntity.status(201).body(Collections.singletonMap("result", "ok"));
        }
    }

    private String hash() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


}
