package myconext.cron;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.*;

public class IdPMetaDataResolverTest {

    @Test
    public void resolveIdpMetaData() {
        IdPMetaDataResolver metaDataResolver = new IdPMetaDataResolver(new ClassPathResource("idps-metadata.xml"));
        metaDataResolver.resolveIdpMetaData();
        assertEquals(316, metaDataResolver.getDomainNames().size());
    }

    @Test
    public void resolveIdpMetaDataNoException() {
        new IdPMetaDataResolver(null);
    }

}