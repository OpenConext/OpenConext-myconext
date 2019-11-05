package surfid.repository;

import org.junit.Before;
import org.junit.Test;
import surfid.AbstractIntegrationTest;
import surfid.exceptions.ExpiredAuthenticationException;
import surfid.model.SamlAuthenticationRequest;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class AuthenticationRequestRepositoryTest extends AbstractIntegrationTest {

    private SamlAuthenticationRequest request;

    @Before
    public void before() throws Exception {
        super.before();
        SamlAuthenticationRequest req = new SamlAuthenticationRequest(
                "requestId", "consumerAssertionServiceURL", "relayState");
        req.setHash("hash");
        request = authenticationRequestRepository.save(req);
    }

    @Test
    public void testDeleteByExpiresInBefore() {
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "requestId", "requestId");

        long deleted = authenticationRequestRepository.deleteByExpiresInBefore(new Date());
        assertEquals(1L, deleted);
    }

    @Test
    public void testFindByHash() {
        assertEquals(true, authenticationRequestRepository.findByHash("hash").isPresent());
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