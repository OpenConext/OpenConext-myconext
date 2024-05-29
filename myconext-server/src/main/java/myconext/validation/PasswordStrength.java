package myconext.validation;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class PasswordStrength {

    private static final Pattern pattern = Pattern.compile("^(((?=.*[A-Z])(?=.*[0-9])(.{8,}))|(.{15,}))$");

    private PasswordStrength() {
    }

    public static boolean strongEnough(String password) {
        return StringUtils.hasText(password) && pattern.matcher(password).matches();
    }
}
