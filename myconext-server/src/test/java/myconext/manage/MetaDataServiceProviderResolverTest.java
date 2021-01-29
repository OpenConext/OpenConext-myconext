package myconext.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.*;

public class MetaDataServiceProviderResolverTest {

    private ServiceProviderResolver subject = new MetaDataServiceProviderResolver(new ClassPathResource("sp_names.json"), new ObjectMapper(), false);

    @Test
    public void resolveLocally() {
        assertEquals("Mine eduID", subject.resolve("http://mijn.localhost/shibboleth").get().getName());
        assertFalse(subject.resolve("https://manage.surfconext.nl/shibboleth").isPresent());
    }

    @Test
    public void doNotRefreshing() {
        new MetaDataServiceProviderResolver(new ClassPathResource("sp_names_wrong.json"), new ObjectMapper(), false);
    }

}