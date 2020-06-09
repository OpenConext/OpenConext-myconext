package myconext.security;

public enum ACR {

    LINKED_INSTITUTION("https://eduid.nl/trust/linked-institution"),
    VALIDATE_NAMES("https://eduid.nl/trust/validate-names");

    private String value;

    private ACR(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }
}
