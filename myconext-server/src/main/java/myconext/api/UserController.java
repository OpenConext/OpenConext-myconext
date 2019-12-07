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
import myconext.validation.EmailValidator;
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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/myconext/api")
public class UserController {

    private UserRepository userRepository;
    private AuthenticationRequestRepository authenticationRequestRepository;
    private MailBox mailBox;
    private ServiceNameResolver serviceNameResolver;
    private String magicLinkUrl;

    private SecureRandom random = new SecureRandom();
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(-1, random);
    private EmailValidator emailValidator = new EmailValidator();
    private EmailGuessingPrevention emailGuessingPreventor = new EmailGuessingPrevention();

    public UserController(UserRepository userRepository,
                          AuthenticationRequestRepository authenticationRequestRepository,
                          MailBox mailBox,
                          ServiceNameResolver serviceNameResolver,
                          @Value("${email.magic-link-url}") String magicLinkUrl) {
        this.userRepository = userRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.mailBox = mailBox;
        this.serviceNameResolver = serviceNameResolver;
        this.magicLinkUrl = magicLinkUrl;
    }

    @PostMapping("/idp/magic_link_request")
    public ResponseEntity newMagicLinkRequest(@Valid @RequestBody MagicLinkRequest magicLinkRequest) throws InterruptedException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);

        User user = magicLinkRequest.getUser();
        Optional<User> userByEmail = userRepository.findUserByEmail(user.getEmail());
        if (userByEmail.isPresent()) {
            emailGuessingPreventor.potentialUserEmailGuess();
            throw new DuplicateUserEmailException();
        }
        emailValidator.validEmail(user.getEmail());

        //prevent not-wanted attributes in the database
        User userToSave = new User(user.getEmail(), user.getGivenName(), user.getFamilyName());
        userToSave.encryptPassword(user.getPassword(), passwordEncoder);
        userToSave = userRepository.save(userToSave);

        return this.doMagicLink(userToSave, samlAuthenticationRequest, magicLinkRequest, false);
    }

    @PutMapping("/idp/magic_link_request")
    public ResponseEntity magicLinkRequest(HttpServletResponse response, @Valid @RequestBody MagicLinkRequest magicLinkRequest) throws InterruptedException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);

        User providedUser = magicLinkRequest.getUser();
        Optional<User> optionalUser = userRepository.findUserByEmail(providedUser.getEmail());
        if (!optionalUser.isPresent()) {
            //Omission in Optional interface for not having ifNotPresent
            emailGuessingPreventor.potentialUserEmailGuess();
            throw new UserNotFoundException();
        }
        User user = optionalUser.get();
        if (magicLinkRequest.isUsePassword()) {
            if (!passwordEncoder.matches(providedUser.getPassword(), user.getPassword())) {
                emailGuessingPreventor.potentialUserEmailGuess();
                throw new ForbiddenException();
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                    user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        return doMagicLink(user, samlAuthenticationRequest, magicLinkRequest, magicLinkRequest.isUsePassword());
    }

    @GetMapping("/sp/me")
    public ResponseEntity me(Authentication authentication) {
        User user = userRepository.findOneUserByEmail(((User) authentication.getPrincipal()).getEmail());
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PutMapping("/sp/update")
    public ResponseEntity updateUserProfile(Authentication authentication, @RequestBody User deltaUser) {
        User user = verifyAndFetchUser(authentication, deltaUser);

        user.setFamilyName(deltaUser.getFamilyName());
        user.setGivenName(deltaUser.getGivenName());
        user.validate();

        userRepository.save(user);

        return ResponseEntity.status(201).body(new UserResponse(user));
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

        return ResponseEntity.status(201).body(new UserResponse(user));
    }


    @DeleteMapping("/sp/delete/{id}")
    public ResponseEntity deleteUser(Authentication authentication, HttpServletRequest request, @PathVariable("id") String id) throws URISyntaxException {
        User user = verifyAndFetchUser(authentication, id);
        userRepository.delete(user);
        HttpSession session = request.getSession();
        session.invalidate();
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(302).location(new URI("/")).build();
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

        authenticationRequestRepository.save(samlAuthenticationRequest);
        if (passwordFlow) {
            return ResponseEntity.status(201).body(Collections.singletonMap("url", this.magicLinkUrl + "?h=" + samlAuthenticationRequest.getHash()));
        } else {
            if (user.isNewUser()) {
                mailBox.sendAccountVerification(user, samlAuthenticationRequest.getHash());
            } else {
                String serviceName = serviceNameResolver.resolve(samlAuthenticationRequest.getRequesterEntityId());
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
