package myconext.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import myconext.model.ExternalLinkedAccount;
import myconext.model.IdpScoping;
import myconext.model.VerifyIssuer;
import myconext.remotecreation.ExternalEduID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
@SuppressWarnings("unchecked")
public class AttributeMapper {

    private static final int DEFAULT_EXPIRATION_YEARS = 6;

    private static final Map<Pattern, DateTimeFormatter> datePatterns = Map.of(
            Pattern.compile("^\\d{8}$"), DateTimeFormatter.ofPattern("yyyyMMdd"), //19750725
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$"), DateTimeFormatter.ofPattern("yyyy-MM-dd") //1963-02-05
    );

    private final ObjectMapper objectMapper;

    @Autowired
    public AttributeMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ExternalLinkedAccount externalLinkedAccountFromAttributes(
            Map<String, Object> attributes,
            VerifyState verifyState) {
        //first determine which attribute set we need to convert
        switch (verifyState.getIdpScoping()) {
            case idin: {
                return new ExternalLinkedAccount(
                        //String subjectId
                        getAttribute(attributes, "sub"),
                        //IdpScoping idpScoping
                        verifyState.getIdpScoping(),
                        //VerifyIssuer issuer
                        verifyState.getVerifyIssuer(),
                        //Verification
                        null,
                        //String serviceUUID
                        getAttribute(attributes, "urn:nl:bvn:bankid:1.0:bankid.deliveredserviceid"),
                        //String serviceID
                        getAttribute(attributes, "urn:nl:bvn:bankid:1.0:bankid.deliveredserviceid"),
                        //String subjectIssuer
                        getAttribute(attributes, "subject_issuer"),
                        //String brinCode
                        null,
                        //String initials
                        getAttribute(attributes, "urn:nl:bvn:bankid:1.0:consumer.initials"),
                        //String chosenName
                        null,
                        //String firstName
                        getAttribute(attributes, "urn:nl:bvn:bankid:1.0:consumer.initials"),
                        //String preferredLastName
                        getAttribute(attributes, "urn:nl:bvn:bankid:1.0:consumer.preferredlastname"),
                        //String legalLastName;
                        getAttribute(attributes, "urn:nl:bvn:bankid:1.0:consumer.legallastname"),
                        //String partnerLastNamePrefix
                        getAttribute(attributes, "urn:nl:bvn:bankid:1.0:consumer.partnerlastnameprefix"),
                        //String legalLastNamePrefix
                        getAttribute(attributes, "urn:nl:bvn:bankid:1.0:consumer.legallastnameprefix"),
                        //String preferredLastNamePrefix
                        getAttribute(attributes, "urn:nl:bvn:bankid:1.0:consumer.preferredlastnameprefix"),
                        //String partnerLastName
                        getAttribute(attributes, "urn:nl:bvn:bankid:1.0:consumer.partnerlastname"),
                        //Date dateOfBirth
                        parseDate(getAttribute(attributes, "urn:nl:bvn:bankid:1.0:consumer.dateofbirth")),
                        //Date createdAt
                        new Date(),
                        //Date expiresAt
                        Date.from(Instant.now().plus(DEFAULT_EXPIRATION_YEARS * 365, ChronoUnit.DAYS)),
                        //boolean external
                        true
                );
            }
            case eherkenning: {
                return new ExternalLinkedAccount(
                        //String subjectId
                        getAttribute(attributes, "sub"),
                        //IdpScoping idpScoping
                        verifyState.getIdpScoping(),
                        //VerifyIssuer issuer
                        verifyState.getVerifyIssuer(),
                        //Verification
                        null,
                        //String serviceUUID
                        getAttribute(attributes, "urn:etoegang:core:ServiceUUID"),
                        //String serviceID
                        getAttribute(attributes, "urn:etoegang:DV:00000003411824080000:services:9001"),
                        //String subjectIssuer
                        getAttribute(attributes, "subject_issuer"),
                        //String brinCode
                        null,
                        //String initials
                        getAttribute(attributes, "urn:etoegang:1.9:attribute:Initials"),
                        //String chosenName
                        null,
                        //String firstName
                        getAttribute(attributes, "urn:etoegang:1.9:attribute:FirstName"),
                        //String preferredLastName
                        getAttribute(attributes, "urn:etoegang:1.9:attribute:FamilyName"),
                        //String legalLastName;
                        getAttribute(attributes, "urn:etoegang:1.9:attribute:FamilyName"),
                        //String partnerLastNamePrefix
                        null,
                        //String legalLastNamePrefix
                        null,
                        //String preferredLastNamePrefix
                        null,
                        //String partnerLastName
                        null,
                        //Date dateOfBirth
                        parseDate(getAttribute(attributes, "urn:etoegang:1.9:attribute:DateOfBirth")),
                        //Date createdAt
                        new Date(),
                        //Date expiresAt
                        Date.from(Instant.now().plus(DEFAULT_EXPIRATION_YEARS * 365, ChronoUnit.DAYS)),
                        //boolean external
                        true
                );
            }
        }
        throw new IllegalArgumentException();
    }

    public ExternalLinkedAccount createExternalLinkedAccount(ExternalEduID eduID, IdpScoping idpScoping) {
        return new ExternalLinkedAccount(
                //String subjectId
                eduID.getIdentifier(),
                //IdpScoping idpScoping
                idpScoping,
                //VerifyIssuer issuer
                new VerifyIssuer(idpScoping.name(), idpScoping.name()),
                //Verification
                eduID.getVerification(),
                //String serviceUUID
                null,
                //String serviceID
                null,
                //String subjectIssuer
                IdpScoping.studielink.name(),
                //String brinCode
                eduID.getBrinCode(),
                //String initials
                null,
                //String chosenName
                eduID.getChosenName(),
                //String firstName
                eduID.getFirstName(),
                //String preferredLastName
                eduID.getLastName(),
                //String legalLastName;
                eduID.getLastName(),
                //String partnerLastNamePrefix
                null,
                //String legalLastNamePrefix
                eduID.getLastNamePrefix(),
                //String preferredLastNamePrefix
                eduID.getLastNamePrefix(),
                //String partnerLastName
                null,
                //Date dateOfBirth
                parseDate(eduID.getDateOfBirth()),
                //Date createdAt
                new Date(),
                //Date expiresAt
                Date.from(Instant.now().plus(DEFAULT_EXPIRATION_YEARS * 365, ChronoUnit.DAYS)),
                //boolean external
                true

        );

    }

    @SneakyThrows
    public String serializeToBase64(VerifyState verifyState) {
        Map<String, String> result = new HashMap<>();
        result.put("i", verifyState.getStateIdentifier());
        result.put("s", verifyState.getIdpScoping().name());
        VerifyIssuer verifyIssuer = verifyState.getVerifyIssuer();
        if (Objects.nonNull(verifyIssuer)) {
            result.put("vi", verifyIssuer.getId());
            result.put("vn", verifyIssuer.getName());
        }

        byte[] bytes = objectMapper.writeValueAsBytes(result);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gout = new GZIPOutputStream(bos);
        gout.write(bytes);
        gout.finish();
        //Avoid decoding / encoding as URL parameter problems
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(bos.toByteArray(), false, true));
    }

    @SneakyThrows
    public VerifyState serializeFromBase64(String base64) {
        byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(base64);
        //Equal or more than 42 KB is considered a gzip bomb attack
        if (decoded.length / 1024 >= 42) {
            throw new IllegalArgumentException("GZip bomb detected");
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(decoded);
        GZIPInputStream gin = new GZIPInputStream(bis);

        Map<String, String> map = objectMapper.readValue(gin, Map.class);
        VerifyIssuer verifyIssuer = null;
        if (map.containsKey("vi") && map.containsKey("vn")) {
            verifyIssuer = new VerifyIssuer(map.get("vi"), map.get("vn"));
        }
        return new VerifyState(
                map.get("i"),
                IdpScoping.valueOf(map.get("s")),
                verifyIssuer
        );
    }

    protected Date parseDate(String dateString) {
        if (StringUtils.hasText(dateString)) {
            DateTimeFormatter dateTimeFormatter = datePatterns.entrySet().stream()
                    .filter(e -> e.getKey().matcher(dateString).matches())
                    .map(e -> e.getValue())
                    .findFirst()
                    .orElse(DateTimeFormatter.ISO_LOCAL_DATE);
            try {
                LocalDate localDate = LocalDate.parse(dateString, dateTimeFormatter);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeException e) {
                //Business decision, don't rethrow but return null, as we don't want to break external account linking
            }
        }
        return null;
    }

    private String getAttribute(Map<String, Object> attributes, String key) {
        return (String) attributes.get(key);
    }

}
