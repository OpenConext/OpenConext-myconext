package myconext.cron;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.exceptions.DisposableEmailProviderException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisposableEmailProvidersTest {

    @Test
    void isDisposableEmailProviders() {
        DisposableEmailProviders subject = new DisposableEmailProviders(new ObjectMapper());
        assertThrows(DisposableEmailProviderException.class, () -> subject.verifyDisposableEmailProviders("jdoe@TRASHMAIL.LIVE"));
        subject.verifyDisposableEmailProviders("jdoe@gmail.com");
    }
}