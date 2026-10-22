package myconext.activity;

import org.junit.Test;

import myconext.manage.Manage;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ActivityContextTest {

    @Test
    public void ofSkipsNullValues() {
        assertTrue(ActivityContext.of(ActivityContext.NAME, null).isEmpty());
    }

    @Test
    public void ofKeepsKeyAndValue() {
        assertEquals(Map.of("name", "MacBook"), ActivityContext.of(ActivityContext.NAME, "MacBook"));
    }

    @Test
    public void keysMatchTheClientPlaceholders() {
        //These are the {{placeholders}} in my-gui/src/locale/*.ts, change them together
        assertEquals("language", ActivityContext.LANGUAGE);
        assertEquals("email", ActivityContext.EMAIL);
        assertEquals("institution", ActivityContext.INSTITUTION);
        assertEquals("name", ActivityContext.NAME);
        assertEquals("service", ActivityContext.SERVICE);
        assertEquals("variant", ActivityContext.VARIANT);
    }

    @Test
    public void institutionFallsBackToIdentifier() {
        Manage manage = mock(Manage.class);
        when(manage.findIdentityProviderByDomainName("surfguest.nl")).thenReturn(Optional.empty());
        assertEquals(Map.of("institution", "surfguest.nl"), ActivityContext.institution("surfguest.nl", manage));
    }

    @Test
    public void institutionNeverFailsOnManageError() {
        Manage manage = mock(Manage.class);
        when(manage.findIdentityProviderByDomainName("surfguest.nl")).thenThrow(new IllegalStateException("down"));
        assertEquals(Map.of("institution", "surfguest.nl"), ActivityContext.institution("surfguest.nl", manage));
    }

    @Test
    public void emptyIdentifierGivesEmptyContext() {
        assertTrue(ActivityContext.service(null, Optional.empty()).isEmpty());
    }
}
