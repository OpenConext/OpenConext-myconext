package myconext.cron;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.exceptions.DisposableEmailProviderException;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DisposableEmailProvidersTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void isDisposableEmailProviders() {
        Environment environment = new MockEnvironment();
        DisposableEmailProviders subject = new DisposableEmailProviders(objectMapper, environment, true);
        assertThrows(DisposableEmailProviderException.class, () -> subject.verifyDisposableEmailProviders("jdoe@TRASHMAIL.WS"));
        subject.verifyDisposableEmailProviders("jdoe@gmail.com");

        DisposableEmailProviders subjectFeatureOff = new DisposableEmailProviders(objectMapper, environment, false);
        subjectFeatureOff.verifyDisposableEmailProviders("jdoe@TRASHMAIL.WS");
    }

    @Test
    void isDisposableEmailProvidersLocalEnvironment() {
        MockEnvironment environment = new MockEnvironment();
        environment.addActiveProfile("test");
        DisposableEmailProviders subject = new DisposableEmailProviders(objectMapper, environment, true);
        assertThrows(DisposableEmailProviderException.class, () -> subject.verifyDisposableEmailProviders("jdoe@UJIXLAXPROS.tech"));
    }
}