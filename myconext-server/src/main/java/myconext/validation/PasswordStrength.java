package myconext.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PasswordStrength {

    //OWASP ASVS V6.2.1: no password shorter than 8 characters
    public static final int MIN_PASSWORD_LENGTH = 8;
    //OWASP ASVS V6.2.9: at least 64 characters must be permitted
    public static final int MAX_PASSWORD_LENGTH = 128;

    private final Set<String> deniedWords;

    @SneakyThrows
    public PasswordStrength(ObjectMapper objectMapper) {
        String resource = "deny-allow/password-denylist.json";
        List<String> deniedWordList = objectMapper.readValue(new ClassPathResource(resource).getInputStream(), new TypeReference<>() {
        });
        this.deniedWords = deniedWordList.stream()
                .map(word -> word.toLowerCase())
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean strongEnough(String password) {
        if (password == null) {
            return false;
        }
        return characterLength(password) >= MIN_PASSWORD_LENGTH && !containsDeniedWord(password);
    }

    public boolean tooLong(String password) {
        return password != null && characterLength(password) > MAX_PASSWORD_LENGTH;
    }

    private int characterLength(String password) {
        return password.codePointCount(0, password.length());
    }

    private boolean containsDeniedWord(String password) {
        String normalizedPassword = password.toLowerCase();
        return deniedWords.stream().anyMatch(normalizedPassword::contains);
    }

}
