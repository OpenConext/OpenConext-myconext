package myconext.security;

import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

public class ACR {

    private ACR() {
    }

    public static String LINKED_INSTITUTION = "https://eduid.nl/trust/linked-institution";
    public static String VALIDATE_NAMES = "https://eduid.nl/trust/validate-names";
    public static String VALIDATE_NAMES_EXTERNAL = "https://eduid.nl/trust/validate-names-external";
    public static String AFFILIATION_STUDENT = "https://eduid.nl/trust/affiliation-student";

    public static List<String> allAccountLinkingContextClassReferences() {
        return Arrays.asList(VALIDATE_NAMES, VALIDATE_NAMES_EXTERNAL, LINKED_INSTITUTION, AFFILIATION_STUDENT);
    }

    public static void initialize(String linkedInstitution,
                                  String validateNames,
                                  String externalValidateNames,
                                  String affiliationStudent) {
        LINKED_INSTITUTION = linkedInstitution;
        VALIDATE_NAMES = validateNames;
        VALIDATE_NAMES_EXTERNAL = externalValidateNames;
        AFFILIATION_STUDENT = affiliationStudent;
    }

    public static String selectACR(List<String> acrValues, boolean studentAffiliationPresent) {
        if (acrValues.contains(VALIDATE_NAMES_EXTERNAL)) {
            return VALIDATE_NAMES_EXTERNAL;
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
