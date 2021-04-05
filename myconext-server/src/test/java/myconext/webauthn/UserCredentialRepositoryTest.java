package myconext.webauthn;

import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.exception.Base64UrlException;
import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserCredentialRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Test
    public void getCredentialIdsForUsername() {
        Set<PublicKeyCredentialDescriptor> credentials = userCredentialRepository.getCredentialIdsForUsername("jdoe@example.com");
        assertEquals(1, credentials.size());

        credentials = userCredentialRepository.getCredentialIdsForUsername("nope@example.com");
        assertEquals(0, credentials.size());
    }

    @Test
    public void getUserHandleForUsername() {
        Optional<ByteArray> userHandle = userCredentialRepository.getUserHandleForUsername("jdoe@example.com");
        assertTrue(userHandle.isPresent());

        userHandle = userCredentialRepository.getUserHandleForUsername("nope@example.com");
        assertFalse(userHandle.isPresent());
    }

    @Test
    public void getUsernameForUserHandle() throws Base64UrlException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        Optional<String> userHandle = userCredentialRepository.getUsernameForUserHandle(ByteArray.fromBase64Url(user.getUserHandle()));
        assertTrue(userHandle.isPresent());

        String base64url = Base64.getEncoder().encodeToString(new String("nope").getBytes());
        userHandle = userCredentialRepository.getUsernameForUserHandle(ByteArray.fromBase64Url(base64url));
        assertFalse(userHandle.isPresent());
    }

    @Test
    public void lookup() throws Base64UrlException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        ByteArray userHandle = ByteArray.fromBase64Url(user.getUserHandle());
        String credentialId = user.getPublicKeyCredentials().iterator().next().getIdentifier();

        Optional<RegisteredCredential> registeredCredential = userCredentialRepository.lookup(ByteArray.fromBase64Url(credentialId), userHandle);
        assertTrue(registeredCredential.isPresent());

        String base64url = Base64.getEncoder().encodeToString(new String("nope").getBytes());
        registeredCredential = userCredentialRepository.lookup(ByteArray.fromBase64Url(base64url), ByteArray.fromBase64Url(base64url));
        assertFalse(registeredCredential.isPresent());

        registeredCredential = userCredentialRepository.lookup(ByteArray.fromBase64Url(base64url), userHandle);
        assertFalse(registeredCredential.isPresent());
    }

    @Test
    public void lookupAll() {
        assertEquals(0, userCredentialRepository.lookupAll(null).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void byteArrayFromBase64Url() {
        userCredentialRepository.byteArrayFromBase64Url("not-base-64-ë%$#@#$");
    }
}