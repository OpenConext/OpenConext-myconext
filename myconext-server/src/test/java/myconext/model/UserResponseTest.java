package myconext.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.AbstractIntegrationTest;
import myconext.manage.MockManage;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserResponseTest {

    @Test
    void getExternalLinkedAccounts() {
        User user = new User();
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount("subject-id", IdpScoping.studielink, true);
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        UserResponse userResponse = new UserResponse(
                user,
                null,
                Optional.empty(),
                false,
                new MockManage(new ObjectMapper()));
        assertEquals(1, userResponse.getExternalLinkedAccounts().size());
    }

    @Test
    void getExternalLinkedAccountsUnverifiedFiltered() {
        User user = new User();
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount("subject-id", IdpScoping.studielink, true);
        externalLinkedAccount.setVerification(Verification.Ongeverifieerd);
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        UserResponse userResponse = new UserResponse(
                user,
                null,
                Optional.empty(),
                false,
                new MockManage(new ObjectMapper()));
        assertEquals(0, userResponse.getExternalLinkedAccounts().size());
    }
}