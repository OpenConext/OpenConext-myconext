package surfid.validation;

import org.apache.commons.codec.language.bm.Rule;
import org.springframework.util.StringUtils;
import surfid.exceptions.WeakPasswordException;

import java.util.regex.Pattern;

public class PasswordStrength {

    private static Pattern pattern = Pattern.compile("^(((?=.*[A-Z])(?=.*[0-9])[a-zA-Z\\d]{8,})|(.{15,}))$");

    public static boolean strongEnough(String password) {
        return StringUtils.hasText(password) && pattern.matcher(password).matches();
    }
}
