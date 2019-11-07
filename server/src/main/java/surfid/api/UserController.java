package surfid.api;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import surfid.exceptions.DuplicateUserEmailException;
import surfid.exceptions.ExpiredAuthenticationException;
import surfid.exceptions.UserNotFoundException;
import surfid.mail.MailBox;
import surfid.model.MagicLinkRequest;
import surfid.model.SamlAuthenticationRequest;
import surfid.model.User;
import surfid.repository.AuthenticationRequestRepository;
import surfid.repository.UserRepository;

import javax.mail.MessagingException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
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

    @PostMapping("/magic_link_request")
    public User newMagicLinkRequest(@Valid @RequestBody MagicLinkRequest magicLinkRequest) throws IOException, MessagingException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);

        User user = magicLinkRequest.getUser();
        Optional<User> userByEmail = userRepository.findUserByEmail(user.getEmail());
        if (userByEmail.isPresent()) {
            throw new DuplicateUserEmailException();
        }
        user.validate();
        user.encryptPassword(passwordEncoder);
        userRepository.save(user);

        return this.doMagicLink(user, samlAuthenticationRequest, magicLinkRequest);
    }

    @PutMapping("/magic_link_request")
    public User magicLinkRequest(@Valid @RequestBody MagicLinkRequest magicLinkRequest) throws IOException, MessagingException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);

        User providedUser = magicLinkRequest.getUser();
        User user = userRepository.findUserByEmail(providedUser.getEmail()).orElseThrow(UserNotFoundException::new);

        return doMagicLink(user, samlAuthenticationRequest, magicLinkRequest);
    }

    private User doMagicLink(User user, SamlAuthenticationRequest samlAuthenticationRequest, MagicLinkRequest magicLinkRequest) throws MessagingException, IOException {
        samlAuthenticationRequest.setHash(hash());
        samlAuthenticationRequest.setUserId(user.getId());
        samlAuthenticationRequest.setRememberMe(magicLinkRequest.isRememberMe());

        authenticationRequestRepository.save(samlAuthenticationRequest);

        mailBox.sendMagicLink(user, samlAuthenticationRequest.getHash());
        return user;
    }

    private String hash() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


}
