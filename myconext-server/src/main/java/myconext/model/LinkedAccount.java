package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @Indexed
    private String eduPersonPrincipalName;
    private String subjectId;
    private String givenName;
    private String familyName;
    private List<String> eduPersonAffiliations = new ArrayList<>();
    private Date createdAt;
    private Date expiresAt;

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

    public void setEduPersonAffiliations(List<String> eduPersonAffiliations) {
        this.eduPersonAffiliations = eduPersonAffiliations;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}
