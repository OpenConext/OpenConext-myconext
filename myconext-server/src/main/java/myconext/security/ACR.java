package myconext.security;

public class ACR {

    private ACR() {
    }

    public static String LINKED_INSTITUTION;
    public static String VALIDATE_NAMES;

    public static void initialize(String linkedInstitution, String validateNames) {
        ACR.LINKED_INSTITUTION = linkedInstitution;
        ACR.VALIDATE_NAMES = validateNames;
    }

}
