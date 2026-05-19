package myconext.util;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class CreateFromInstitutionReturnUrlSupport {

    private CreateFromInstitutionReturnUrlSupport() {
    }

    public static Optional<String> validateAndNormalize(String returnUrl, List<String> allowedDomains) {
        if (!StringUtils.hasText(returnUrl)) {
            return Optional.empty();
        }
        if (allowedDomains == null || allowedDomains.isEmpty()) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(returnUrl.trim());
            String scheme = Optional.ofNullable(uri.getScheme()).orElse("").toLowerCase(Locale.ROOT);
            String host = Optional.ofNullable(uri.getHost()).orElse("").toLowerCase(Locale.ROOT);
            if (!("http".equals(scheme) || "https".equals(scheme)) || !StringUtils.hasText(host)) {
                return Optional.empty();
            }
            boolean allowed = allowedDomains.stream()
                    .filter(StringUtils::hasText)
                    .map(domain -> domain.trim().toLowerCase(Locale.ROOT))
                    .anyMatch(domain -> host.equals(domain) || host.endsWith("." + domain));
            return allowed ? Optional.of(uri.toString()) : Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
