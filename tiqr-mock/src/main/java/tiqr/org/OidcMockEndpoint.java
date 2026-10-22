package tiqr.org;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Minimal OIDC provider for the institution linking flows of myconext-server in the e2e tests. Start the server
 * with oidc.base-url pointing to this application. The authorize page offers a persona per link scenario, the
 * persona is carried in the code and access token, so this endpoint has no state.
 */
@RestController
public class OidcMockEndpoint {

    private static final String SCHAC_HOME = "institution.example";
    // The eppn of a linked account of another user in the e2e seed (myconext-server/src/main/resources/e2e/users.json)
    private static final String LINKED_EPPN = "taken@institution.example";

    private static final List<String> PERSONAS = List.of("success", "no-attributes", "eppn-already-linked");

    @GetMapping(value = "/oidc/authorize", produces = MediaType.TEXT_HTML_VALUE)
    public String authorize(HttpServletRequest request,
                            @RequestParam("redirect_uri") String redirectUri) {
        // The state is echoed unchanged, the server decodes it itself
        String rawState = rawQueryParameter(request.getQueryString(), "state");
        StringBuilder html = new StringBuilder("<html><head><title>Mock IdP</title></head><body><h1>Mock IdP</h1><ul>");
        for (String persona : PERSONAS) {
            String code = persona + "." + UUID.randomUUID();
            String location = redirectUri + (redirectUri.contains("?") ? "&" : "?") + "code=" + code + "&state=" + rawState;
            html.append(String.format("<li><a data-testid=\"oidc-persona-%s\" href=\"%s\">%s</a></li>",
                    persona, HtmlUtils.htmlEscape(location), persona));
        }
        return html.append("</ul></body></html>").toString();
    }

    @PostMapping(value = "/oidc/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> token(@RequestParam("code") String code) {
        return Map.of("access_token", code, "token_type", "Bearer", "expires_in", 3600);
    }

    @PostMapping(value = "/oidc/userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> userInfo(@RequestParam("access_token") String accessToken) {
        String[] parts = accessToken.split("\\.", 2);
        String persona = parts[0];
        String unique = parts.length > 1 ? parts[1] : UUID.randomUUID().toString();

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", "urn:collab:person:" + SCHAC_HOME + ":" + unique);
        claims.put("given_name", "Eva");
        claims.put("family_name", "Tester");
        claims.put("name", "Eva Tester");
        claims.put("email", "eva-" + unique + "@" + SCHAC_HOME);
        claims.put("schac_home_organization", SCHAC_HOME);
        claims.put("eduperson_affiliation", List.of("student"));
        claims.put("eduperson_scoped_affiliation", List.of("student@" + SCHAC_HOME));
        switch (persona) {
            case "success" -> {
                claims.put("eduperson_principal_name", unique + "@" + SCHAC_HOME);
                claims.put("subject_id", unique + "@" + SCHAC_HOME);
            }
            case "eppn-already-linked" -> claims.put("eduperson_principal_name", LINKED_EPPN);
            default -> {
                // no eppn and subject_id: the attributes are missing
            }
        }
        return claims;
    }

    private String rawQueryParameter(String queryString, String name) {
        if (queryString == null) {
            return "";
        }
        for (String pair : queryString.split("&")) {
            if (pair.startsWith(name + "=")) {
                return pair.substring(name.length() + 1);
            }
        }
        return "";
    }
}
