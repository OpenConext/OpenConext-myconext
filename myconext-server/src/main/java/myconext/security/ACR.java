package myconext.security;

import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

public class ACR {

    private ACR() {
    }

    public static String LINKED_INSTITUTION = "https://eduid.nl/trust/linked-institution";
    public static String VALIDATE_NAMES = "https://eduid.nl/trust/validate-names";
    public static String AFFILIATION_STUDENT = "https://eduid.nl/trust/affiliation-student";
    public static String PROFILE_MFA = "https://refeds.org/profile/mfa";

    public static List<String> allAccountLinkingContextClassReferences() {
        return Arrays.asList(VALIDATE_NAMES, LINKED_INSTITUTION, AFFILIATION_STUDENT);
    }

    public static void initialize(String linkedInstitution,
                                  String validateNames,
                                  String affiliationStudent,
                                  String profileMfa) {
        LINKED_INSTITUTION = linkedInstitution;
        VALIDATE_NAMES = validateNames;
        AFFILIATION_STUDENT = affiliationStudent;
        PROFILE_MFA = profileMfa;
    }

    public static String selectACR(List<String> acrValues, boolean studentAffiliationPresent) {
        if (acrValues.contains(PROFILE_MFA)) {
            return PROFILE_MFA;
        }
        if (acrValues.contains(VALIDATE_NAMES)) {
            return VALIDATE_NAMES;
        }
        if (acrValues.contains(AFFILIATION_STUDENT) && studentAffiliationPresent) {
            return AFFILIATION_STUDENT;
        }
        return LINKED_INSTITUTION;
    }

    public static String explanationKeyWord(List<String> acrValues, boolean studentAffiliationPresent) {
        if (CollectionUtils.isEmpty(acrValues)) {
            return "linked_institution";
        }
        if (acrValues.contains(PROFILE_MFA)) {
            return "profile_mfa";
        }
        if (acrValues.contains(VALIDATE_NAMES)) {
            return "validate_names";
        }
        if (acrValues.contains(AFFILIATION_STUDENT) && studentAffiliationPresent) {
            return "affiliation_student";
        }
        return "linked_institution";
    }

}
