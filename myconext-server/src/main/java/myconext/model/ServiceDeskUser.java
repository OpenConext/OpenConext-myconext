package myconext.model;

import java.util.List;

/**
 * Summary of a {@link User} as shown in the service desk user overview
 */
public record ServiceDeskUser(String id,
                              String uid,
                              String name,
                              String email,
                              boolean blocked,
                              List<String> mfaMethods,
                              List<String> affiliations) {

    public static final String MFA_APP = "app";
    public static final String MFA_PASSKEY = "passkey";
    public static final String MFA_PASSWORD = "password";

}
