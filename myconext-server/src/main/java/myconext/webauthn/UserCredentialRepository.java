package myconext.webauthn;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.exception.Base64UrlException;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class UserCredentialRepository implements CredentialRepository {

    private UserRepository userRepository;

    @Autowired
    public UserCredentialRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String email) {
        Optional<User> userOptional = userRepository.findUserByEmailIgnoreCase(email);
        return userOptional.map(user -> user.getPublicKeyCredentials().keySet().stream()
                .map(id ->
                        PublicKeyCredentialDescriptor.builder()
                                .id(byteArrayFromBase64Url(id))
                                .type(PublicKeyCredentialType.PUBLIC_KEY)
                                .build())
                .collect(Collectors.toSet())).orElse(Collections.emptySet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String email) {
        return userRepository.findUserByEmailIgnoreCase(email)
                .map(user -> byteArrayFromBase64Url(user.getUserHandle()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return userRepository.findUserByUserHandle(userHandle.getBase64Url()).map(user -> user.getEmail());
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        String credentialKey = credentialId.getBase64Url();
        Optional<User> optionalUser = userRepository.findUserByUserHandle(userHandle.getBase64Url());
        //Deliberately non-functional to improve code readability
        if (!optionalUser.isPresent()) {
            return Optional.empty();
        }
        Map<String, String> publicKeyCredentials = optionalUser.get().getPublicKeyCredentials();
        String publicKeyCose = publicKeyCredentials.get(credentialKey);
        if (StringUtils.isEmpty(publicKeyCose)) {
            return Optional.empty();
        }
        RegisteredCredential registeredCredential = RegisteredCredential.builder()
                .credentialId(credentialId)
                .userHandle(userHandle)
                .publicKeyCose(byteArrayFromBase64Url(publicKeyCose))
                .build();
        return Optional.of(registeredCredential);
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return Collections.emptySet();
    }

    private ByteArray byteArrayFromBase64Url(String base64Url) {
        try {
            return ByteArray.fromBase64Url(base64Url);
        } catch (Base64UrlException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
