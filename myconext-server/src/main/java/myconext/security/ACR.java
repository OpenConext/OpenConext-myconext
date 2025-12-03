package myconext.security;

import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

public class ACR {

    private ACR() {
    }

    public static String MFA = "/mfa";

    public static String LINKED_INSTITUTION = "https://eduid.nl/trust/linked-institution"; //student, staff, faculty, affiliate
    public static String VALIDATE_NAMES = "https://eduid.nl/trust/validate-names";
    public static String VALIDATE_NAMES_EXTERNAL = "https://eduid.nl/trust/validate-names-external";
    public static String AFFILIATION_STUDENT = "https://eduid.nl/trust/affiliation-student"; // only student
    public static String PROFILE_MFA = "https://refeds.org/profile/mfa";

    public static String LINKED_INSTITUTION_MFA = LINKED_INSTITUTION + MFA;
    public static String VALIDATE_NAMES_MFA = VALIDATE_NAMES + MFA;
    public static String VALIDATE_NAMES_EXTERNAL_MFA = VALIDATE_NAMES_EXTERNAL + MFA;
    public static String AFFILIATION_STUDENT_MFA = AFFILIATION_STUDENT + MFA;

    public static List<String> allAccountLinkingContextClassReferences() {
        return Arrays.asList(VALIDATE_NAMES, VALIDATE_NAMES_EXTERNAL, LINKED_INSTITUTION, AFFILIATION_STUDENT);
    }

    public static void initialize(String linkedInstitution,
                                  String validateNames,
                                  String externalValidateNames,
                                  String affiliationStudent,
                                  String profileMfa) {
        LINKED_INSTITUTION = linkedInstitution;
        VALIDATE_NAMES = validateNames;
        VALIDATE_NAMES_EXTERNAL = externalValidateNames;
        AFFILIATION_STUDENT = affiliationStudent;
        PROFILE_MFA = profileMfa;
        LINKED_INSTITUTION_MFA = linkedInstitution + MFA;
        VALIDATE_NAMES_MFA = validateNames + MFA;
        VALIDATE_NAMES_EXTERNAL_MFA = externalValidateNames + MFA;
        AFFILIATION_STUDENT_MFA = affiliationStudent + MFA;
    }

    public static String selectACR(List<String> acrValues, boolean studentAffiliationPresent) {
        List<String> priorityOrder = Arrays.asList(
            VALIDATE_NAMES_EXTERNAL_MFA,
            VALIDATE_NAMES_MFA,
            AFFILIATION_STUDENT_MFA,
            LINKED_INSTITUTION_MFA,
            PROFILE_MFA,
            VALIDATE_NAMES_EXTERNAL,
            VALIDATE_NAMES,
            AFFILIATION_STUDENT,
            LINKED_INSTITUTION
        );

        for (String acr : priorityOrder) {
            if ((acr.equals(AFFILIATION_STUDENT_MFA) || acr.equals(AFFILIATION_STUDENT))) {
                if(studentAffiliationPresent && acrValues.contains(acr)) {
                    return acr;
                }
                // without studentAffiliationPresent the ACR is not selected
                continue;
            }

            if (acrValues.contains(acr)) {
                return acr;
            }
        }

        return LINKED_INSTITUTION;
    }

    public static String explanationKeyWord(List<String> acrValues, boolean studentAffiliationPresent) {
        if (CollectionUtils.isEmpty(acrValues) || acrValues.contains(LINKED_INSTITUTION)) {
            return "linked_institution";
        }
        if (acrValues.contains(PROFILE_MFA)) {
            return "profile_mfa";
        }
        if (acrValues.contains(VALIDATE_NAMES_EXTERNAL)) {
            return "validate_names_external";
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
