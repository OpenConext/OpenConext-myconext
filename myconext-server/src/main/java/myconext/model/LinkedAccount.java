package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.beans.Transient;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LinkedAccount {

    private String institutionIdentifier;
    private String eduPersonPrincipalName;
    private Date expiresAt;

    @Transient
    @JsonIgnore
    public boolean updateExpiresIn(String institutionIdentifier, Date expiresAt) {
        this.institutionIdentifier = institutionIdentifier;
        this.expiresAt = expiresAt;
        return true;
    }
}
