package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.beans.Transient;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LinkedAccount {

    private String institutionIdentifier;
    private String schacHomeOrganization;
    private String eduPersonPrincipalName;
    private String givenName;
    private String familyName;
    private List<String> eduPersonAffiliations;
    private Date createdAt;
    private Date expiresAt;

    @Transient
    @JsonIgnore
    public boolean updateExpiresIn(String institutionIdentifier, List<String> eduPersonAffiliations, Date expiresAt) {
        this.institutionIdentifier = institutionIdentifier;
        this.eduPersonAffiliations = eduPersonAffiliations;
        this.expiresAt = expiresAt;
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
}
