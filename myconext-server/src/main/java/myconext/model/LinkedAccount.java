package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.util.StringUtils;

import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LinkedAccount implements Serializable {

    private String institutionIdentifier;
    private String schacHomeOrganization;
    @Setter
    private String displayNameEn;
    @Setter
    private String displayNameNl;
    @Setter
    private String logoUrl;
    @Indexed
    private String eduPersonPrincipalName;
    private String subjectId;
    private String givenName;
    private String familyName;
    @Setter
    private List<String> eduPersonAffiliations = new ArrayList<>();
    @Setter
    private boolean preferred;
    @Setter
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date createdAt;
    @Setter
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date expiresAt;
    private boolean external = false;
    @Setter
    private String institutionGuid;

    public LinkedAccount(String institutionIdentifier,
                         String schacHomeOrganization,
                         String eduPersonPrincipalName,
                         String subjectId,
                         String givenName,
                         String familyName,
                         List<String> eduPersonAffiliations,
                         boolean preferred,
                         Date createdAt,
                         Date expiresAt) {
        this.institutionIdentifier = institutionIdentifier;
        this.schacHomeOrganization = StringUtils.hasText(schacHomeOrganization) ? schacHomeOrganization.toLowerCase() : schacHomeOrganization ;
        this.eduPersonPrincipalName = eduPersonPrincipalName;
        this.subjectId = subjectId;
        this.givenName = givenName;
        this.familyName = familyName;
        this.eduPersonAffiliations = eduPersonAffiliations;
        this.preferred = preferred;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    @Transient
    @JsonIgnore
    public boolean updateExpiresIn(String institutionIdentifier, String eppn, String subjectId, String givenName, String familyName, List<String> eduPersonAffiliations, Date expiresAt) {
        this.institutionIdentifier = institutionIdentifier;
        this.eduPersonPrincipalName = eppn;
        this.subjectId = subjectId;
        this.givenName = givenName;
        this.familyName = familyName;
        this.eduPersonAffiliations = eduPersonAffiliations;
        this.expiresAt = expiresAt;
        this.createdAt = new Date();
        return true;
    }

    @Transient
    @JsonIgnore
    public boolean areNamesValidated() {
        return StringUtils.hasText(givenName) && StringUtils.hasText(familyName);
    }

}
