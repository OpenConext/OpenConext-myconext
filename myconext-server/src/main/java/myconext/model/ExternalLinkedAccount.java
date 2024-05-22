package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ExternalLinkedAccount implements Serializable {

    private String subjectId;
    private IdpScoping idpScoping;
    private VerifyIssuer issuer;
    private String serviceUUID;
    private String serviceID;
    private String subjectIssuer;

    private String initials;
    private String firstName;
    private String preferredLastName;
    private String legalLastName;
    private String partnerLastNamePrefix;
    private String legalLastNamePrefix;
    private String preferredLastNamePrefix;
    private String partnerLastName;
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
    }
}
