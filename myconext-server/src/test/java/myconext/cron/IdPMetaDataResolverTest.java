package myconext.cron;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Set;

import static org.junit.Assert.*;

public class IdPMetaDataResolverTest {

    @Test
    public void resolveIdpMetaData() {
        IdPMetaDataResolver metaDataResolver = new IdPMetaDataResolver(new ClassPathResource("metadata/idps-metadata.xml"));
        metaDataResolver.resolveIdpMetaData();
        Set<String> domainNames = metaDataResolver.getDomainNames();
        assertEquals(2, domainNames.size());
    }

    @Test
    public void resolveIdpMetaDataNoException() {
        new IdPMetaDataResolver(null);
    }

}