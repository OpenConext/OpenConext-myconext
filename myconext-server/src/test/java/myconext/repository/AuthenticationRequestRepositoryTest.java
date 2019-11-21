package myconext.repository;

import org.junit.Before;
import org.junit.Test;
import myconext.AbstractIntegrationTest;
import myconext.exceptions.ExpiredAuthenticationException;
import myconext.model.SamlAuthenticationRequest;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class AuthenticationRequestRepositoryTest extends AbstractIntegrationTest {

    private SamlAuthenticationRequest request;

    @Before
    public void before() throws Exception {
        super.before();
        SamlAuthenticationRequest req = new SamlAuthenticationRequest(
                "requestId", "consumerAssertionServiceURL", "relayState", "http://mock-sp");
        req.setHash(UUID.randomUUID().toString());
        request = authenticationRequestRepository.save(req);

        SamlAuthenticationRequest reqRememberMe = new SamlAuthenticationRequest(
                "requestId", "consumerAssertionServiceURL", "relayState", "http://mock-sp");
        reqRememberMe.setHash("differentHash");
        reqRememberMe.setRememberMe(true);
        authenticationRequestRepository.save(reqRememberMe);
    }

    @Test
    public void testDeleteByExpiresInBefore() {
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "requestId", "requestId");

        long deleted = authenticationRequestRepository.deleteByExpiresInBeforeAndRememberMe(new Date(), false);
        assertEquals(1L, deleted);
    }

    @Test
    public void testFindByHash() {
        Optional<SamlAuthenticationRequest> hash = authenticationRequestRepository.findByHash(request.getHash());
        assertEquals(true, hash.isPresent());
    }

    @Test(expected = ExpiredAuthenticationException.class)
    public void testFindByIdAndNotExpired() {
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "requestId", "requestId");
        authenticationRequestRepository.findByIdAndNotExpired(request.getId());
    }

    @Test
    public void testFindByIdAndExpired() {
        assertEquals(true, authenticationRequestRepository.findByIdAndNotExpired(request.getId()).isPresent());
    }
}