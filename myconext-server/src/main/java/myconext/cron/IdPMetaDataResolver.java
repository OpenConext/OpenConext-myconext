package myconext.cron;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

@Component
public class IdPMetaDataResolver {

    private static final Log LOG = LogFactory.getLog(IdPMetaDataResolver.class);

    private Resource metaDataResource;
    private Set<String> domainNames = new HashSet<>();

    @Autowired
    public IdPMetaDataResolver(@Value("${cron.metadata-resolver-url}") Resource metaDataResource) {
        this.metaDataResource = metaDataResource;
    }

    @Scheduled(initialDelayString = "${cron.metadata-resolver-initial-delay-milliseconds}",
            fixedRateString = "${cron.metadata-resolver-fixed-rate-milliseconds}")
    public void resolveIdpMetaData() {
        XMLStreamReader reader;
        Set<String> newDomainNames = new HashSet<>();
        try {
            reader = getXMLStreamReader(metaDataResource);
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == START_ELEMENT) {
                    String startLocalName = reader.getLocalName();
                    if ("Scope".equals(startLocalName)) {
                        newDomainNames.add(reader.getElementText());
                    }
                }
            }
            this.domainNames = newDomainNames;
            LOG.info(String.format("Parsed %s domain names from %s", domainNames.size(), metaDataResource.getDescription()));
        } catch (Exception e) {
            LOG.error("Error in resolveIdpMetaData", e);
        }
    }

    public Set<String> getDomainNames() {
        if (domainNames.isEmpty()) {
            resolveIdpMetaData();
        }
        return domainNames;
    }

    private XMLStreamReader getXMLStreamReader(Resource xml) throws IOException, XMLStreamException {
        //despite it's name, the XMLInputFactoryImpl is not thread safe
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
        xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
        return xmlInputFactory.createXMLStreamReader(xml.getInputStream());
    }

}
