package myconext.security;

import java.util.Arrays;
import java.util.List;

public class ACR {

    private ACR() {
    }

    public static String LINKED_INSTITUTION;
    public static String VALIDATE_NAMES;
    public static String AFFILIATION_STUDENT;

    public static List<String> all() {
        return Arrays.asList(ACR.VALIDATE_NAMES, ACR.LINKED_INSTITUTION, ACR.AFFILIATION_STUDENT);
    }

    public static void initialize(String linkedInstitution, String validateNames, String affiliationStudent) {
        ACR.LINKED_INSTITUTION = linkedInstitution;
        ACR.VALIDATE_NAMES = validateNames;
        ACR.AFFILIATION_STUDENT = affiliationStudent;
    }

}
