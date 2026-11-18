package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document(collection = "authentication_requests")
@Getter
@NoArgsConstructor
public class SamlAuthenticationRequest implements Serializable {

    @Id
    private String id;

    private String requestId;

    private String issuer;

    private String consumerAssertionServiceURL;

    private String relayState;

    @Setter
    @Indexed
    private String hash;

    @Schema(type = "integer", format = "int64", example = "1634813554997")
    @Indexed(name = "expiresIn_ttl_idx", expireAfter = "0s", partialFilter = "{ 'rememberMe': false }")
    private Date expiresIn;

    //The user this request is (or will be) bound to. Set as soon as the user identifies themselves (e.g. enters an email)
    //and therefore NOT proof of authentication. Use userAuthenticated for that.
    private String userId;

    //True only after the user bound in userId has actually authenticated (code, password, passkey, app or existing
    //SSO session). Endpoints that act on behalf of userId based on the (non-secret) id of this request must check this.
    private boolean userAuthenticated;

    private String requesterEntityId;

    private boolean accountLinkingRequired;

    private boolean mfaProfileRequired;

    private List<String> authenticationContextClassReferences;

    @Setter
    private boolean passwordOrWebAuthnFlow;

    @Setter
    private boolean oneTimeLoginCodeFlow;

    @Setter
    private boolean tiqrFlow;

    @Setter
    private boolean rememberMe;

    @Setter
    @Indexed
    private String rememberMeValue;

    @Setter
    private StepUpStatus steppedUp = StepUpStatus.NONE;

    private boolean testInstance;

    //The GUI base url that started the flow, as flows can finish on another host without our session: the test passkey
    //flow (myconext GUI) or a login flow (account-gui or login-gui)
    @Setter
    private String guiRedirectUrl;

    @Setter
    private LoginStatus loginStatus = LoginStatus.NOT_LOGGED_IN;

    @Setter
    private String verificationCode;

    @Setter
    private int retryVerificationCode;

    @Setter
    private String serviceName;

    @Setter
    private boolean rememberMeQuestionAsked = false;

    public SamlAuthenticationRequest(boolean testInstance) {
        this.id = UUID.randomUUID().toString();
        this.testInstance = testInstance;
        this.requesterEntityId = "test";
        this.authenticationContextClassReferences = new ArrayList<>();
        this.expiresIn = Date.from(LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toInstant());
    }

    public SamlAuthenticationRequest(String requestId,
                                     String issuer,
                                     String consumerAssertionServiceURL,
                                     String relayState,
                                     String requesterEntityId,
                                     boolean accountLinkingRequired,
                                     boolean mfaProfileRequired,
                                     List<String> authenticationContextClassReferences) {
        this.id = UUID.randomUUID().toString();
        this.requestId = requestId;
        this.issuer = issuer;
        this.consumerAssertionServiceURL = consumerAssertionServiceURL;
        this.relayState = relayState;
        this.expiresIn = Date.from(LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toInstant());
        this.requesterEntityId = requesterEntityId;
        this.accountLinkingRequired = accountLinkingRequired;
        this.mfaProfileRequired = mfaProfileRequired;
        this.authenticationContextClassReferences = authenticationContextClassReferences;
        invariant();
    }

    /**
     * Bind a (candidate) user to this request. Always revokes any previous proof of authentication, so an
     * authenticated request can never be re-bound to another user while keeping its authenticated state.
     */
    public void setUserId(String userId) {
        this.userId = userId;
        this.userAuthenticated = false;
    }

    /**
     * Mark the currently bound user as authenticated. Only call this after the user has proven possession of a
     * credential (or has an existing authenticated session).
     */
    public void markUserAuthenticated() {
        if (this.userId == null) {
            throw new IllegalStateException("Can not mark a SamlAuthenticationRequest authenticated without a user");
        }
        this.userAuthenticated = true;
    }

    @Transient
    private void invariant() {
        if ((this.isAccountLinkingRequired() || this.isMfaProfileRequired()) &&
                CollectionUtils.isEmpty(this.authenticationContextClassReferences)) {
            throw new IllegalArgumentException("authenticationContextClassReference is required when account linking or mfa profiles is required");
        }
    }

}
