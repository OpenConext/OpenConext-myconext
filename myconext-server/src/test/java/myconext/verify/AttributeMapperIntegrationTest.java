package myconext.verify;

import myconext.AbstractIntegrationTest;
import myconext.model.ExternalLinkedAccount;
import myconext.model.User;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeMapperIntegrationTest extends AbstractIntegrationTest {

    @Test
    void parseDateWithTimeZone() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount();
        Date dateOfBirth = AttributeMapper.parseDate("1991-12-18");
        externalLinkedAccount.setDateOfBirth(dateOfBirth);
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        userRepository.save(user);

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(dateOfBirth, userFromDB.getExternalLinkedAccounts().get(0).getDateOfBirth());
        assertEquals("Wed Dec 18 00:00:00 CET 1991", dateOfBirth.toString());
    }


}