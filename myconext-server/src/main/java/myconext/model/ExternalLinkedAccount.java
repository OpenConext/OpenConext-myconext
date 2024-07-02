package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@NoArgsConstructor
@Getter
public class ExternalLinkedAccount implements Serializable {

    private String subjectId;
    private IdpScoping idpScoping;
    private VerifyIssuer issuer;
    @Setter
    private Verification verification;
    private String serviceUUID;
    private String serviceID;
    private String subjectIssuer;
    @Setter
    private String brinCode;

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
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date createdAt;
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date expiresAt;
    private boolean external = true;

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
                                 String brinCode,
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
        this.brinCode = brinCode;
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

}
