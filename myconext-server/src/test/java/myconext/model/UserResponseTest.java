package myconext.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.manage.MockManage;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class UserResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<VerifyIssuer> issuers;

    {
        try {
            List<IdinIssuers> idinIssuers = new ObjectMapper().readValue(new ClassPathResource("/idin/issuers.json")
                    .getInputStream(), new TypeReference<>() {
            });
            this.issuers = idinIssuers.getFirst().getIssuers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void getExternalLinkedAccounts() {
        User user = new User();
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount("subject-id", IdpScoping.studielink, true);
        externalLinkedAccount.setCreatedAt(new Date());
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

    @Test
    @SuppressWarnings("unchecked")
    void nullMapKey() throws JsonProcessingException {
        Map<String, EduID> eduidServiceProvider =
                new HashMap<>();
        eduidServiceProvider.put(null, new EduID());
        eduidServiceProvider.put("key", null);
        UserResponse userResponse = new UserResponse(new User(), eduidServiceProvider, Optional.empty(), false,
                new MockManage(objectMapper), emptyList());
        Map<String, Object> parsedJson = objectMapper.readValue(objectMapper.writeValueAsString(userResponse), new TypeReference<>() {
        });
        Map<String, EduID> eduIdPerServiceProvider = (Map<String, EduID>) parsedJson.get("eduIdPerServiceProvider");
        assertTrue(eduIdPerServiceProvider.isEmpty());
    }
}