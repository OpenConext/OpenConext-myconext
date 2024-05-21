package myconext.verify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import myconext.model.ExternalLinkedAccount;
import myconext.model.IdpScoping;
import myconext.model.VerifyIssuer;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AttributeMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AttributeMapper attributeMapper = new AttributeMapper(objectMapper);

    @SneakyThrows
    @Test
    void externalLinkedAccountFromAttributesIdin() {
        Map<String, Object> attributes = attributesFromFile("verify/idin.json");
        String stateIdentifier = UUID.randomUUID().toString();
        VerifyIssuer verifyIssuer = new VerifyIssuer("RABONL2U", "Rabobank NL");
        VerifyState verifyState = new VerifyState(stateIdentifier, IdpScoping.idin, verifyIssuer);
        ExternalLinkedAccount externalLinkedAccount = attributeMapper.externalLinkedAccountFromAttributes(attributes, verifyState);
        //Verify entire mapping
        assertEquals("FANTASYBANK1234567890", externalLinkedAccount.getSubjectId());
        assertEquals(IdpScoping.idin, externalLinkedAccount.getIdpScoping());
        assertEquals(verifyIssuer, externalLinkedAccount.getIssuer());
        assertEquals("", externalLinkedAccount.getServiceID());
        assertEquals("", externalLinkedAccount.getServiceUUID());
        assertEquals("idin", externalLinkedAccount.getSubjectIssuer());
        assertEquals("VJ", externalLinkedAccount.getInitials());
        assertEquals("VJ", externalLinkedAccount.getFirstName());
        assertEquals("Vries-Jansen", externalLinkedAccount.getPreferredLastName());
        assertEquals("Vries", externalLinkedAccount.getLegalLastName());
        assertEquals("van", externalLinkedAccount.getPartnerLastNamePrefix());
        assertEquals("de", externalLinkedAccount.getLegalLastNamePrefix());
        assertEquals("Jansen", externalLinkedAccount.getPartnerLastName());
        assertEquals(attributeMapper.parseDate("19750725"), externalLinkedAccount.getDateOfBirth());
        assertNotNull(externalLinkedAccount.getCreatedAt());
        assertNotNull(externalLinkedAccount.getExpiresAt());
    }

    @SneakyThrows
    @Test
    void externalLinkedAccountFromAttributesEHerkenning() {
        Map<String, Object> attributes = attributesFromFile("verify/eherkenning.json");
        String stateIdentifier = UUID.randomUUID().toString();
        VerifyState verifyState = new VerifyState(stateIdentifier, IdpScoping.eherkenning, null);
        ExternalLinkedAccount externalLinkedAccount = attributeMapper.externalLinkedAccountFromAttributes(attributes, verifyState);
        //Verify entire mapping
        assertEquals("BGEuxX6iGQR2i2XNU2A3GHFYLtJ5Dmehf2aQ+qDQUJ4AbjgX+j9+1DEuhUK4sRJ5AwMa0CV2xQyf93xrU/8yqfzTtxc6c+wsYPzWF9tqux9T", externalLinkedAccount.getSubjectId());
        assertEquals(IdpScoping.eherkenning, externalLinkedAccount.getIdpScoping());
        assertEquals(null, externalLinkedAccount.getIssuer());
        assertEquals(null, externalLinkedAccount.getServiceID());
        assertEquals("a23aed9b-e310-495e-adea-561a1b07f333", externalLinkedAccount.getServiceUUID());
        assertEquals("urn:etoegang:HM:00000003244440010000:entities:9713", externalLinkedAccount.getSubjectIssuer());
        assertEquals("M.", externalLinkedAccount.getInitials());
        assertEquals("Mariana", externalLinkedAccount.getFirstName());
        assertEquals("Kjällström", externalLinkedAccount.getPreferredLastName());
        assertEquals("Kjällström", externalLinkedAccount.getLegalLastName());
        assertEquals(null, externalLinkedAccount.getPartnerLastNamePrefix());
        assertEquals(null, externalLinkedAccount.getLegalLastNamePrefix());
        assertEquals(null, externalLinkedAccount.getPartnerLastName());
        assertEquals(attributeMapper.parseDate("1963-02-05"), externalLinkedAccount.getDateOfBirth());
        assertNotNull(externalLinkedAccount.getCreatedAt());
        assertNotNull(externalLinkedAccount.getExpiresAt());
    }

    @Test
    void parseDate() {
        Date eherkenningDate = attributeMapper.parseDate("1963-02-21");
        Date idinDate = attributeMapper.parseDate("19630221");
        assertEquals(eherkenningDate, idinDate);
    }

    @Test
    void serializeToBase64() {
        String stateIdentifier = UUID.randomUUID().toString();
        VerifyState verifyState = new VerifyState(stateIdentifier, IdpScoping.idin, new VerifyIssuer("RABONL2U", "Rabobank NL"));
        String serialized = attributeMapper.serializeToBase64(verifyState);

        VerifyState verifyStateParsed = attributeMapper.serializeFromBase64(serialized);

        assertEquals(verifyState.getStateIdentifier(), verifyStateParsed.getStateIdentifier());
        assertEquals(verifyState.getIdpScoping(), verifyStateParsed.getIdpScoping());
        assertEquals(verifyState.getVerifyIssuer(), verifyStateParsed.getVerifyIssuer());
    }

    @Test
    void serializeToBase64WithNulls() {
        String stateIdentifier = UUID.randomUUID().toString();
        VerifyState verifyState = new VerifyState(stateIdentifier, IdpScoping.idin, null);
        String serialized = attributeMapper.serializeToBase64(verifyState);

        VerifyState verifyStateParsed = attributeMapper.serializeFromBase64(serialized);

        assertEquals(verifyState.getStateIdentifier(), verifyStateParsed.getStateIdentifier());
        assertEquals(verifyState.getIdpScoping(), verifyStateParsed.getIdpScoping());
        assertEquals(verifyState.getVerifyIssuer(), verifyStateParsed.getVerifyIssuer());
    }


    @Test
    void serializeFromBase64GZipBomb() {
        String s = Base64.getEncoder().encodeToString(new byte[42 * 1024]);
        assertThrows(IllegalArgumentException.class, () -> attributeMapper.serializeFromBase64(s));

    }

    private Map<String, Object> attributesFromFile(String path) throws IOException {
        return objectMapper.readValue(IOUtils.toString(new ClassPathResource(path).getInputStream(), Charset.defaultCharset()), new TypeReference<>() {
        });
    }


}