package surfid.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import surfid.exceptions.DuplicateUserEmailException;
import surfid.exceptions.ExpiredAuthenticationException;
import surfid.exceptions.ForbiddenException;
import surfid.exceptions.UserNotFoundException;
import surfid.mail.MailBox;
import surfid.model.MagicLinkRequest;
import surfid.model.SamlAuthenticationRequest;
import surfid.model.User;
import surfid.repository.AuthenticationRequestRepository;
import surfid.repository.UserRepository;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/surfid/api")
public class UserController {

    private UserRepository userRepository;
    private AuthenticationRequestRepository authenticationRequestRepository;
    private MailBox mailBox;

    private SecureRandom random = new SecureRandom();
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(-1, random);

    public UserController(UserRepository userRepository, AuthenticationRequestRepository authenticationRequestRepository, MailBox mailBox) {
        this.userRepository = userRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.mailBox = mailBox;
    }

    @PostMapping("/idp/magic_link_request")
    public ResponseEntity newMagicLinkRequest(@Valid @RequestBody MagicLinkRequest magicLinkRequest) throws IOException, MessagingException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);

        User user = magicLinkRequest.getUser();
        Optional<User> userByEmail = userRepository.findUserByEmail(user.getEmail());
        if (userByEmail.isPresent()) {
            throw new DuplicateUserEmailException();
        }

        User userToSave = new User(user.getEmail());
        //prevent not-wanted attributes in the database
        userToSave.merge(user, passwordEncoder);
        userToSave = userRepository.save(userToSave);

        return this.doMagicLink(userToSave, samlAuthenticationRequest, magicLinkRequest);
    }

    @PutMapping("/idp/magic_link_request")
    public ResponseEntity magicLinkRequest(@Valid @RequestBody MagicLinkRequest magicLinkRequest) throws IOException, MessagingException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);

        User providedUser = magicLinkRequest.getUser();
        User user = userRepository.findUserByEmail(providedUser.getEmail()).orElseThrow(UserNotFoundException::new);

        if (magicLinkRequest.isUsePassword()) {
            if (!passwordEncoder.matches(providedUser.getPassword(), user.getPassword())) {
                throw new ForbiddenException();
            }
        }

        return doMagicLink(user, samlAuthenticationRequest, magicLinkRequest);
    }

    @PutMapping("/sp/update")
    public ResponseEntity updateUser(Authentication authentication, @RequestBody User deltaUser) {
        User user = verifyAndFetchUser(authentication, deltaUser);
        user.merge(deltaUser, passwordEncoder);
        userRepository.save(user);

        return ResponseEntity.status(201).body(user);
    }

    @PutMapping("/sp/delete")
    public ResponseEntity deleteUser(Authentication authentication, @Valid @RequestBody User deltaUser) {
        User user = verifyAndFetchUser(authentication, deltaUser);
        userRepository.delete(user);

        return ResponseEntity.status(201).build();
    }

    private User verifyAndFetchUser(Authentication authentication, User deltaUser) {
        User principal = (User) authentication.getPrincipal();
        if (!principal.getId().equals(deltaUser.getId())) {
            throw new ForbiddenException();
        }
        //Strictly not necessary, but mid-air collisions can occur in theory
        return userRepository.findOneUserByEmail(principal.getEmail());
    }

    private ResponseEntity doMagicLink(User user, SamlAuthenticationRequest samlAuthenticationRequest, MagicLinkRequest magicLinkRequest) throws MessagingException, IOException {
        samlAuthenticationRequest.setHash(hash());
        samlAuthenticationRequest.setUserId(user.getId());
        samlAuthenticationRequest.setRememberMe(magicLinkRequest.isRememberMe());

        authenticationRequestRepository.save(samlAuthenticationRequest);

        mailBox.sendMagicLink(user, samlAuthenticationRequest.getHash());
        return ResponseEntity.status(201).body(Collections.singletonMap("result", "ok"));
    }

    private String hash() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


}
