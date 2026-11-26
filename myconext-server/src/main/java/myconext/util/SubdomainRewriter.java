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
