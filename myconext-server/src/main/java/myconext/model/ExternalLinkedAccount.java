package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@ToString
public class ExternalLinkedAccount implements Serializable, ProvisionedLinkedAccount {

    private String subjectId;
    private IdpScoping idpScoping;
    @Setter
    private VerifyIssuer issuer;
    @Setter
    private Verification verification;
    private String serviceUUID;
    private String serviceID;
    private String subjectIssuer;
    @Setter
    private List<String> brinCodes;

    private String initials;
    private String chosenName;
    private String firstName;
    private String preferredLastName;
    private String legalLastName;
    private String partnerLastNamePrefix;
    private String legalLastNamePrefix;
    private String preferredLastNamePrefix;
    private String partnerLastName;

    @Setter
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date dateOfBirth;
    @Setter
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date createdAt;
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date expiresAt;
    private boolean external = true;
    @Setter
    private boolean preferred;

    public ExternalLinkedAccount(String subjectId, IdpScoping idpScoping, boolean external) {
        this.subjectId = subjectId;
        this.idpScoping = idpScoping;
        this.external = external;
        this.expiresAt = Date.from(Instant.now().plus(5 * 365, ChronoUnit.DAYS));
    }

    public ExternalLinkedAccount(String subjectId,
                                 IdpScoping idpScoping,
                                 VerifyIssuer issuer,
                                 Verification verification,
                                 String serviceUUID,
                                 String serviceID,
                                 String subjectIssuer,
                                 List<String> brinCodes,
                                 String initials,
                                 String chosenName,
                                 String firstName,
                                 String preferredLastName,
                                 String legalLastName,
                                 String partnerLastNamePrefix,
                                 String legalLastNamePrefix,
                                 String preferredLastNamePrefix,
                                 String partnerLastName,
                                 Date dateOfBirth,
                                 Date createdAt,
                                 Date expiresAt,
                                 boolean external) {
        this.subjectId = subjectId;
        this.idpScoping = idpScoping;
        this.issuer = issuer;
        this.verification = verification;
        this.serviceUUID = serviceUUID;
        this.serviceID = serviceID;
        this.subjectIssuer = subjectIssuer;
        this.brinCodes = brinCodes;
        this.initials = initials;
        this.chosenName = chosenName;
        this.firstName = firstName;
        this.preferredLastName = preferredLastName;
        this.legalLastName = legalLastName;
        this.partnerLastNamePrefix = partnerLastNamePrefix;
        this.legalLastNamePrefix = legalLastNamePrefix;
        this.preferredLastNamePrefix = preferredLastNamePrefix;
        this.partnerLastName = partnerLastName;
        this.dateOfBirth = dateOfBirth;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.external = external;

        this.nameInvariant();
    }

    private void nameInvariant() {
        if (StringUtils.hasText(this.preferredLastName) && StringUtils.hasText(this.preferredLastNamePrefix)) {
            this.preferredLastName = String.format("%s %s", this.preferredLastNamePrefix, this.preferredLastName);
        }
        if (StringUtils.hasText(this.legalLastName) && StringUtils.hasText(this.legalLastNamePrefix)) {
            this.legalLastName = String.format("%s %s", this.legalLastNamePrefix, this.legalLastName);
        }
    }

    public boolean areNamesValidated() {
        switch (this.idpScoping) {
            case idin:
                return StringUtils.hasText(initials) && StringUtils.hasText(legalLastName);
            case eherkenning:
                return StringUtils.hasText(firstName) && StringUtils.hasText(preferredLastName);
            case studielink:
                return StringUtils.hasText(firstName) && StringUtils.hasText(legalLastName) && !Verification.Ongeverifieerd.equals(verification);
            default:
                throw new IllegalArgumentException("Won't happen");
        }
    }

    @JsonIgnore
    @Transient
    public ExternalLinkedAccount logoReference(List<VerifyIssuer> issuers) {
        if (this.issuer != null) {
            issuers.stream()
                    .filter(issuer -> issuer.getId().equals(this.issuer.getId()))
                    .findFirst()
                    .ifPresent(verifyIssuer -> this.issuer.setLogo(verifyIssuer.getLogo()));
        }
        return this;
    }

    @Override
    public String getGivenName() {
        //idin only returns initial
        if (StringUtils.hasText(firstName) && IdpScoping.eherkenning.equals(idpScoping)) {
            return firstName;
        }
        if (StringUtils.hasText(initials) && IdpScoping.idin.equals(idpScoping)) {
            return initials;
        }
        return null;
    }

    @Override
    public String getFamilyName() {
        if (StringUtils.hasText(legalLastName)) {
            return legalLastName;
        }
        if (StringUtils.hasText(preferredLastName)) {
            return preferredLastName;
        }
        return null;
    }
}
