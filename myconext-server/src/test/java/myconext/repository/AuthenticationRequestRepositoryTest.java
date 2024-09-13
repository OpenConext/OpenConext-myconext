package myconext.repository;

import myconext.AbstractIntegrationTest;
import myconext.exceptions.ExpiredAuthenticationException;
import myconext.model.SamlAuthenticationRequest;
import myconext.security.ACR;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuthenticationRequestRepositoryTest extends AbstractIntegrationTest {

    private SamlAuthenticationRequest request;

    @Before
    public void before() throws Exception {
        super.before();
        SamlAuthenticationRequest req = samlAuthenticationRequest();
        req.setHash(UUID.randomUUID().toString());
        request = authenticationRequestRepository.save(req);

        SamlAuthenticationRequest reqRememberMe = samlAuthenticationRequest();
        reqRememberMe.setHash("differentHash");
        reqRememberMe.setRememberMe(true);
        authenticationRequestRepository.save(reqRememberMe);
    }

    private SamlAuthenticationRequest samlAuthenticationRequest() {
        return new SamlAuthenticationRequest(
                "requestId", "issuer", "consumerAssertionServiceURL",
                "relayState", "http://mock-sp", false, false, Collections.singletonList(ACR.LINKED_INSTITUTION));
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
        assertTrue(hash.isPresent());
    }

    @Test(expected = ExpiredAuthenticationException.class)
    public void testFindByIdAndNotExpired() {
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "requestId", "requestId");
        authenticationRequestRepository.findByIdAndNotExpired(request.getId())
                .orElseThrow(() -> new ExpiredAuthenticationException("Expired"));
    }

    @Test
    public void testFindByIdAndNotExpiredButRememberMe() {
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "hash", "differentHash");
        assertTrue(authenticationRequestRepository.findByIdAndNotExpired(request.getId()).isPresent());
    }

    @Test
    public void testFindByIdAndExpired() {
        assertTrue(authenticationRequestRepository.findByIdAndNotExpired(request.getId()).isPresent());
    }

    @Test(expected = IncorrectResultSizeDataAccessException.class)
    public void testFindByHashWithNullValue() {
        //Two AuthenticationRequests with null values
        authenticationRequestRepository.save(samlAuthenticationRequest());
        authenticationRequestRepository.save(samlAuthenticationRequest());
        authenticationRequestRepository.findByHash(null);

    }
}