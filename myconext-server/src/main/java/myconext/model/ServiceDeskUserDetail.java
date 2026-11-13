package myconext.model;

import java.util.Date;
import java.util.List;

/**
 * All details of a {@link User} as shown in the service desk user details page
 *
 * @param verifiedBy        the {@link IdpScoping} names of the external accounts that verified the identity of the user
 * @param namesVerified     if the given and family name are verified by an external account
 * @param dateOfBirth       the (verified) date of birth, null if not known
 * @param activities        all recorded activities, most recent first
 */
public record ServiceDeskUserDetail(String id,
                                    String uid,
                                    String eduPersonPrincipalName,
                                    String email,
                                    String givenName,
                                    String familyName,
                                    String chosenName,
                                    Date dateOfBirth,
                                    boolean namesVerified,
                                    boolean blocked,
                                    List<String> mfaMethods,
                                    List<String> affiliations,
                                    List<String> verifiedBy,
                                    List<Activity> activities) {
}
