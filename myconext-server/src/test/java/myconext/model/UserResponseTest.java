package myconext.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.manage.MockManage;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserResponseTest {

    private final List<VerifyIssuer> issuers;

    {
        try {
            List<IdinIssuers> idinIssuers = new ObjectMapper().readValue(new ClassPathResource("/idin/issuers.json")
                    .getInputStream(), new TypeReference<>() {

            });
            this.issuers = idinIssuers.get(0).getIssuers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void getExternalLinkedAccounts() {
        User user = new User();
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount("subject-id", IdpScoping.studielink, true);
        ExternalLinkedAccount abnAmroLinkedAccount = new ExternalLinkedAccount("subject-id", IdpScoping.idin, true);
        abnAmroLinkedAccount.setIssuer(new VerifyIssuer("ABNANL2A", "ABN AMRO", null));
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        user.getExternalLinkedAccounts().add(abnAmroLinkedAccount);
        UserResponse userResponse = new UserResponse(
                user,
                null,
                Optional.empty(),
                false,
                new MockManage(new ObjectMapper()),
                issuers);
        assertEquals(2, userResponse.getExternalLinkedAccounts().size());
        ExternalLinkedAccount abnAmroAccount = userResponse.getExternalLinkedAccounts().stream().filter(account -> account.getIssuer() != null).findFirst().get();
        assertNotNull(abnAmroAccount.getIssuer().getLogo());
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
                new MockManage(new ObjectMapper()),
                issuers);
        assertEquals(0, userResponse.getExternalLinkedAccounts().size());
    }
}