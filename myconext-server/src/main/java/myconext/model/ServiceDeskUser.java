package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Summary of a {@link User} as shown in the service desk user overview
 */
@JsonIgnoreProperties("eduPersonPrincipalName")
public record ServiceDeskUser(String id,
                              String uid,
                              String name,
                              String email,
                              boolean blocked,
                              List<String> mfaMethods,
                              List<String> affiliations,
                              List<String> roles,
                              String eduPersonPrincipalName) {

    public ServiceDeskUser withRoles(List<String> roles) {
        return new ServiceDeskUser(id, uid, name, email, blocked, mfaMethods, affiliations, roles, eduPersonPrincipalName);
    }

    public static final String MFA_APP = "app";
    public static final String MFA_PASSKEY = "passkey";
    public static final String MFA_PASSWORD = "password";

}
