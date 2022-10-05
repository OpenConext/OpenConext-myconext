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
        return Arrays.asList(ACR.VALIDATE_NAMES, ACR.LINKED_INSTITUTION, ACR.AFFILIATION_STUDENT);
    }

    public static void initialize(String linkedInstitution,
                                  String validateNames,
                                  String affiliationStudent,
                                  String profileMfa) {
        ACR.LINKED_INSTITUTION = linkedInstitution;
        ACR.VALIDATE_NAMES = validateNames;
        ACR.AFFILIATION_STUDENT = affiliationStudent;
        ACR.PROFILE_MFA = profileMfa;
    }

    public static String selectACR(List<String> acrValues, boolean studentAffiliationPresent) {
        if (acrValues.contains(ACR.PROFILE_MFA)) {
            return PROFILE_MFA;
        }
        if (acrValues.contains(ACR.VALIDATE_NAMES)) {
            return VALIDATE_NAMES;
        }
        if (acrValues.contains(ACR.AFFILIATION_STUDENT) && studentAffiliationPresent) {
            return ACR.AFFILIATION_STUDENT;
        }
        return ACR.LINKED_INSTITUTION;
    }

    public static String explanationKeyWord(List<String> acrValues, boolean studentAffiliationPresent) {
        if (acrValues.contains(ACR.PROFILE_MFA)) {
            return "profile_mfa";
        }
        if (CollectionUtils.isEmpty(acrValues)) {
            return "linked_institution";
        }
        if (acrValues.contains(ACR.VALIDATE_NAMES)) {
            return "validate_names";
        }
        if (acrValues.contains(ACR.AFFILIATION_STUDENT) && studentAffiliationPresent) {
            return "affiliation_student";
        }
        return "linked_institution";
    }

}
