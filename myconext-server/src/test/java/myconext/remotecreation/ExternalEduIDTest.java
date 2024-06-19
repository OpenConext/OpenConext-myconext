package myconext.remotecreation;

import myconext.AbstractIntegrationTest;
import myconext.model.Verification;
import org.junit.jupiter.api.Test;

import javax.validation.ValidationException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ExternalEduIDTest extends AbstractIntegrationTest {

    @Test
    void validate() {
        assertThrows(ValidationException.class, () ->
                new NewExternalEduID(
                        "john@example.com",
                        "chosenName",
                        null,
                        null,
                        "lastName",
                        null,
                        UUID.randomUUID().toString(),
                        Verification.Geverifieerd,
                        "brinCode"
                ).validate());
    }

    @Test
    void validateOngeverifieerd() {
        new NewExternalEduID(
                "john@example.com",
                "chosenName",
                null,
                null,
                null,
                null,
                UUID.randomUUID().toString(),
                Verification.Ongeverifieerd,
                "brinCode"
        ).validate();
    }

}