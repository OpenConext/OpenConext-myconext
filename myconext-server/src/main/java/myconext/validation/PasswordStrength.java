package myconext.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PasswordStrength {

    private static final Pattern pattern = Pattern.compile("^(((?=.*[A-Z])(?=.*[0-9])(.{8,}))|(.{15,}))$");
    private  final Set<String> deniedWords ;

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
        return StringUtils.hasText(password)
                && pattern.matcher(password).matches()
                && !containsDeniedWord(password);
    }

    private boolean containsDeniedWord(String password) {
        String normalizedPassword = password.toLowerCase();
        return deniedWords.stream().anyMatch(normalizedPassword::contains);
    }

}
