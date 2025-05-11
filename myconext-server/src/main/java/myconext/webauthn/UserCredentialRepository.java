package myconext.webauthn;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.exception.Base64UrlException;
import myconext.model.PublicKeyCredentials;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserCredentialRepository implements CredentialRepository {

    private final UserRepository userRepository;

    @Autowired
    public UserCredentialRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String email) {
        Optional<User> userOptional = userRepository.findUserByEmailAndRateLimitedFalse(email.trim());
        return userOptional.map(user -> user.getPublicKeyCredentials().stream()
                .map(publicKeyCredentials ->
                        PublicKeyCredentialDescriptor.builder()
                                .id(byteArrayFromBase64Url(publicKeyCredentials.getIdentifier()))
                                .type(PublicKeyCredentialType.PUBLIC_KEY)
                                .build())
                .collect(Collectors.toSet())).orElse(Collections.emptySet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String email) {
        return userRepository.findUserByEmailAndRateLimitedFalse(email.trim())
                .map(user -> byteArrayFromBase64Url(user.getUserHandle()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return userRepository.findUserByUserHandle(userHandle.getBase64Url()).map(User::getEmail);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        String credentialKey = credentialId.getBase64Url();
        Optional<User> optionalUser = userRepository.findUserByUserHandle(userHandle.getBase64Url());
        return optionalUser.map(user ->
                        user.getPublicKeyCredentials()
                                .stream()
                                .filter(publicKeyCredential -> publicKeyCredential.getIdentifier().equals(credentialKey))
                                .map(PublicKeyCredentials::getCredential)
                                .filter(StringUtils::hasText)
                                .findFirst())
                .flatMap(Function.identity())
                .map(publicKeyCose -> RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(userHandle)
                        .publicKeyCose(byteArrayFromBase64Url(publicKeyCose))
                        .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return Collections.emptySet();
    }

    protected ByteArray byteArrayFromBase64Url(String base64Url) {
        try {
            return ByteArray.fromBase64Url(base64Url);
        } catch (Base64UrlException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
