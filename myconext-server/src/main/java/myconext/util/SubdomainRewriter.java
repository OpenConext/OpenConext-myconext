package myconext.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public final class SubdomainRewriter {

    private static final Log LOG = LogFactory.getLog(SubdomainRewriter.class);

    private static final List<String> SUBDOMAINS_TO_POSTFIX = List.of("login", "mijn");
    private static final String NEW_POSTFIX_TEXT = "-nieuw";

    private SubdomainRewriter() {
    }

    /**
     * Moves the scheme, host and port of {@code url} to those of {@code baseUrl}. User info, path, query and fragment
     * of {@code url} are kept. A blank or unparsable value, or a base without a host, returns {@code url} unchanged.
     * E.g. the server's magic link, so it is reached through the host of the GUI that handles the request.
     */
    public static String moveToBase(String url, String baseUrl) {
        if (!StringUtils.hasText(url) || !StringUtils.hasText(baseUrl)) {
            return url;
        }
        try {
            URI uri = new URI(url);
            URI base = new URI(baseUrl);
            if (uri.getHost() == null || base.getHost() == null) {
                return url;
            }
            return new URI(base.getScheme(), uri.getUserInfo(), base.getHost(), base.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            LOG.debug("Could not move url " + url + " to base " + baseUrl, e);
            return url;
        }
    }

    /**
     * Same rewrite as {@link #appendNewToSubdomain(String)}, but for a bare host name without scheme (e.g. a WebAuthn rp.id).
     * A host that is not rewritten (e.g. localhost), or a blank or unparsable value, is returned unchanged.
     */
    public static String appendNewToHost(String host) {
        if (!StringUtils.hasText(host)) {
            return host;
        }
        // The scheme is only there so URI parses the value as a host; it is stripped again below.
        String rewritten = appendNewToSubdomain("https://" + host);
        try {
            String newHost = new URI(rewritten).getHost();
            return newHost != null ? newHost : host;
        } catch (URISyntaxException e) {
            LOG.debug("Could not rewrite host: " + host, e);
            return host;
        }
    }

    /**
     * Appends {@link #NEW_POSTFIX_TEXT} to the first host label of the url when that label is one of {@link #SUBDOMAINS_TO_POSTFIX}.
     * Scheme, port, path, query and fragment are kept. Any other host (e.g. localhost, servicedesk), or a blank or
     * unparsable url, is returned unchanged.
     */
    public static String appendNewToSubdomain(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) {
                return url;
            }
            int dot = host.indexOf('.');
            if (dot < 0) {
                return url;
            }
            String subdomain = host.substring(0, dot);
            if (!SUBDOMAINS_TO_POSTFIX.contains(subdomain)) {
                return url;
            }
            String newHost = subdomain + NEW_POSTFIX_TEXT + host.substring(dot);
            return new URI(uri.getScheme(), uri.getUserInfo(), newHost, uri.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            LOG.debug("Could not rewrite url: " + url, e);
            return url;
        }
    }
}
