package myconext.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String DENYLIST_RESOURCE = "deny-allow/password-denylist.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<String> deniedWords = loadDeniedWords();

    private PasswordStrength() {
    }

    public static boolean strongEnough(String password) {
        return StringUtils.hasText(password)
                && pattern.matcher(password).matches()
                && !containsDeniedWord(password);
    }

    private static boolean containsDeniedWord(String password) {
        String normalizedPassword = password.toLowerCase(Locale.ROOT);
        return deniedWords.stream()
                .map(word -> word.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedPassword::contains);
    }

    private static Set<String> loadDeniedWords() {
        try (InputStream inputStream = new ClassPathResource(DENYLIST_RESOURCE).getInputStream()) {
            List<String> words = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            return words.stream()
                    .filter(StringUtils::hasText)
                    .map(word -> word.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load password denylist from " + DENYLIST_RESOURCE, e);
        }
    }
}
