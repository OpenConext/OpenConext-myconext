package myconext.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.assertEquals;

public class ServiceNameResolverTest {

    private ServiceNameResolver subject = new ServiceNameResolver(new ClassPathResource("sp_names.json"), new ObjectMapper(), false);

    @Test
    public void resolveLocally() {
        assertEquals("SURFconext Manage | SURFconext", subject.resolve("https://manage.surfconext.nl/shibboleth"));
    }

    @Test
    public void doNotRefreshing() {
        new ServiceNameResolver(new ClassPathResource("sp_names_wrong.json"), new ObjectMapper(), false);
    }

}