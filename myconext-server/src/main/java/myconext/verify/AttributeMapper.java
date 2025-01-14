package myconext.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import myconext.manage.Manage;
import myconext.model.ExternalLinkedAccount;
import myconext.model.IdpScoping;
import myconext.model.Verification;
import myconext.model.VerifyIssuer;
import myconext.remotecreation.NewExternalEduID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
@SuppressWarnings("unchecked")
public class AttributeMapper {

    private static final int DEFAULT_EXPIRATION_YEARS = 6;

    private static final List<DateTimeFormatter> dateTimeFormatters;

    static {
        String pattern = String.join("", List.of(
                "[yyyy-MM-dd]",
                "[dd MMM yyyy]",
                "[dd-MMM-yyyy]",
                "[yyyyMMdd]"));
        dateTimeFormatters = List.of(
                new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .parseLenient()
                        .appendPattern(pattern).toFormatter(Locale.ENGLISH),
                new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .parseLenient()
                        .appendPattern(pattern).toFormatter(Locale.of("nl"))
        );
    }

    private final ObjectMapper objectMapper;
    private final Manage manage;

    @Autowired
    public AttributeMapper(ObjectMapper objectMapper, Manage manage) {
        this.objectMapper = objectMapper;
        this.manage = manage;
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
                        Verification.Decentraal,
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
                        Verification.Decentraal,
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

    public ExternalLinkedAccount createExternalLinkedAccount(NewExternalEduID eduID, IdpScoping idpScoping) {
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount(
                //String subjectId
                eduID.getIdentifier(),
                //IdpScoping idpScoping
                idpScoping,
                //VerifyIssuer issuer
                new VerifyIssuer(idpScoping.name(), idpScoping.name(), null),
                //Verification
                eduID.getVerification(),
                //String serviceUUID
                null,
                //String serviceID
                null,
                //String subjectIssuer
                IdpScoping.studielink.name(),
                //String brinCode
                eduID.getBrinCodes(),
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
        externalLinkedAccount.setAffiliations(externalAffiliations(eduID.getBrinCodes(), manage));
        return externalLinkedAccount;
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
            verifyIssuer = new VerifyIssuer(map.get("vi"), map.get("vn"), null);
        }
        return new VerifyState(
                map.get("i"),
                IdpScoping.valueOf(map.get("s")),
                verifyIssuer
        );
    }

    public static Date parseDate(String dateString) {
        if (StringUtils.hasText(dateString)) {
            //Final variables are required in functional code
            AtomicReference<Date> atomicReference = new AtomicReference<>();
            dateTimeFormatters.forEach(dateTimeFormatter -> {
                try {
                    LocalDate localDate = LocalDate.parse(dateString, dateTimeFormatter);
                    atomicReference.set(Date.from(localDate.atStartOfDay(ZoneOffset.UTC).toInstant()));
                } catch (DateTimeException e) {
                    //Business decision, don't rethrow but return null, as we don't want to break external account linking
                }
            });
            return atomicReference.get();
        }
        return null;
    }

    public static List<String> externalAffiliations(List<String> brinCodes, Manage manage) {
        return CollectionUtils.isEmpty(brinCodes) ? Collections.emptyList() :
                brinCodes.stream()
                        .map(manage::findIdentityProviderByBrinCode)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(idp -> StringUtils.hasText(idp.getDomainName()))
                        .map(idp -> String.format("student@%s", idp.getDomainName()))
                        .toList();
    }


    private String getAttribute(Map<String, Object> attributes, String key) {
        return (String) attributes.get(key);
    }

}
