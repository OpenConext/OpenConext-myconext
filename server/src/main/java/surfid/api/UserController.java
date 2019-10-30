package surfid.api;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import surfid.mail.MailBox;
import surfid.model.SamlAuthenticationRequest;
import surfid.model.User;
import surfid.repository.AuthenticationRequestRepository;
import surfid.repository.UserRepository;

import javax.mail.MessagingException;
import javax.validation.Valid;
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

    public UserController(UserRepository userRepository, AuthenticationRequestRepository authenticationRequestRepository, MailBox mailBox) {
        this.userRepository = userRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.mailBox = mailBox;
    }

    @GetMapping("/user")
    public User userByEmail(@RequestParam("email") String email) {
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        return optionalUser.orElseThrow(() -> new UsernameNotFoundException(email));
    }

    @PostMapping("/user")
    public void save(@Valid @RequestBody User user) throws IOException, MessagingException {
        Optional<User> optionalUser = userRepository.findUserByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            throw new DuplicateKeyException(user.getEmail());
        }
        String authenticationRequestId = optionalUser.get().getAuthenticationRequestId();
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId);
        SamlAuthenticationRequest authenticationRequest = optionalSamlAuthenticationRequest.orElseThrow(() -> new AccountExpiredException(authenticationRequestId));
        authenticationRequest.setHash(hash());

        authenticationRequestRepository.save(authenticationRequest);

        mailBox.sendMagicLink(user, authenticationRequest.getHash());
    }

    private String hash() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


}
