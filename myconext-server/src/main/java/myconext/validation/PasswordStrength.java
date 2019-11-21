package myconext.validation;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class PasswordStrength {

    private static Pattern pattern = Pattern.compile("^(((?=.*[A-Z])(?=.*[0-9])[a-zA-Z\\d]{8,})|(.{15,}))$");

    public static boolean strongEnough(String password) {
        return StringUtils.hasText(password) && pattern.matcher(password).matches();
    }
}
