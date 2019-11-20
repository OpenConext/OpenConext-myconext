package surfid.cron;

import org.junit.Test;
import surfid.AbstractIntegrationTest;
import surfid.model.SamlAuthenticationRequest;

import static org.junit.Assert.assertEquals;

public class ResourceCleanerTest extends AbstractIntegrationTest {

    @Test
    public void clean() {
        doTest(true, 0);
    }

    @Test
    public void notClean() {
        doTest(false, 1);
    }

    private void doTest(boolean b, int i) {
        ResourceCleaner resourceCleaner = new ResourceCleaner(authenticationRequestRepository, b);
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "id", "1");
        resourceCleaner.clean();

        assertEquals(i, authenticationRequestRepository.findAll().size());
    }
}