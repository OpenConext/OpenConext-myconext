package myconext.validation;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class EmailValidator {

    private static Pattern pattern = Pattern.compile("^(.+)@(.+){2,}\\.(.+){2,}$");

    public boolean validEmail(String email) {
        return StringUtils.hasText(email) && pattern.matcher(email).matches();
    }
}
