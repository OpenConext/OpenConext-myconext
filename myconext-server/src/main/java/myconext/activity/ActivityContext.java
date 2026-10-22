package myconext.activity;

import myconext.manage.Manage;
import myconext.model.RemoteProvider;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The language-neutral details of an activity. The keys are the <code>{{placeholders}}</code> in the
 * <code>activity.events.*</code> translations of the client, so changing a key requires changing the locales.
 */
public final class ActivityContext {

    /** The new preferred language code, e.g. nl */
    public static final String LANGUAGE = "language";
    /** The new email address */
    public static final String EMAIL = "email";
    /** The name of an institution, or the idpScoping (idin, eherkenning, ...) of an external account. Falls back to the identifier */
    public static final String INSTITUTION = "institution";
    /** The Dutch name of the institution, only if known */
    public static final String INSTITUTION_NL = "institutionNl";
    /** The name of the user, or of a passkey */
    public static final String NAME = "name";
    /** The name of a service. Falls back to the entityId */
    public static final String SERVICE = "service";
    /** The Dutch name of the service, only if known */
    public static final String SERVICE_NL = "serviceNl";
    /** Distinguishes events with a different meaning, e.g. recoveryCode or phone */
    public static final String VARIANT = "variant";

    public static final String VARIANT_RECOVERY_CODE = "recoveryCode";
    public static final String VARIANT_PHONE = "phone";

    private ActivityContext() {
    }

    public static Map<String, String> none() {
        return new LinkedHashMap<>();
    }

    public static Map<String, String> of(String key, String value) {
        Map<String, String> context = new LinkedHashMap<>();
        if (value != null) {
            context.put(key, value);
        }
        return context;
    }

    /**
     * The institution with its names from Manage, the identifier is used if Manage does not know the institution.
     */
    public static Map<String, String> institution(String identifier, Manage manage) {
        return remote(INSTITUTION, INSTITUTION_NL, identifier, () -> manage.findIdentityProviderByDomainName(identifier));
    }

    /**
     * The service with its names, the entityId is used if the service is not known.
     */
    public static Map<String, String> service(String entityId, Optional<? extends RemoteProvider> service) {
        return remote(SERVICE, SERVICE_NL, entityId, () -> service);
    }

    private static Map<String, String> remote(String key, String keyNl, String identifier,
                                              Supplier<Optional<? extends RemoteProvider>> lookup) {
        Map<String, String> context = new LinkedHashMap<>();
        if (!StringUtils.hasText(identifier)) {
            return context;
        }
        Optional<? extends RemoteProvider> provider;
        try {
            provider = lookup.get();
        } catch (RuntimeException e) {
            //Manage is only needed for a nice name, never fail the user action
            provider = Optional.empty();
        }
        context.put(key, provider.map(RemoteProvider::getName).filter(StringUtils::hasText).orElse(identifier));
        provider.map(RemoteProvider::getNameNl).filter(StringUtils::hasText).ifPresent(nl -> context.put(keyNl, nl));
        return context;
    }
}
