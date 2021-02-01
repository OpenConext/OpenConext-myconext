package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.saml.saml2.authentication.AuthenticationContextClassReference;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document(collection = "authentication_requests")
@Getter
@NoArgsConstructor
public class SamlAuthenticationRequest {

    @Id
    private String id;

    private String requestId;

    private String issuer;

    private String consumerAssertionServiceURL;

    private String relayState;

    private String hash;

    private Date expiresIn;

    private String userId;

    private String requesterEntityId;

    private boolean accountLinkingRequired;

    private List<String> authenticationContextClassReferences;

    private boolean passwordOrWebAuthnFlow;

    private boolean rememberMe;

    private String rememberMeValue;

    private StepUpStatus steppedUp = StepUpStatus.NONE;

    private boolean testInstance;

    private LoginStatus loginStatus = LoginStatus.NOT_LOGGED_IN;

    private String verificationCode;

    public SamlAuthenticationRequest(boolean testInstance) {
        this.id = UUID.randomUUID().toString();
        this.testInstance = testInstance;
        this.requesterEntityId = "test";
        this.authenticationContextClassReferences = new ArrayList<>();
        this.expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

    }

    public SamlAuthenticationRequest(String requestId, String issuer, String consumerAssertionServiceURL,
                                     String relayState, String requesterEntityId,
                                     boolean accountLinkingRequired,
                                     List<String> authenticationContextClassReferences) {
        this.id = UUID.randomUUID().toString();
        this.requestId = requestId;
        this.issuer = issuer;
        this.consumerAssertionServiceURL = consumerAssertionServiceURL;
        this.relayState = relayState;
        this.expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        this.requesterEntityId = requesterEntityId;
        this.accountLinkingRequired = accountLinkingRequired;
        this.authenticationContextClassReferences = authenticationContextClassReferences;
        invariant();
    }

    @Transient
    private void invariant() {
        if (this.isAccountLinkingRequired() && CollectionUtils.isEmpty(this.authenticationContextClassReferences)) {
            throw new IllegalArgumentException("authenticationContextClassReference is required when account linking is required");
        }
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public void setRememberMeValue(String rememberMeValue) {
        this.rememberMeValue = rememberMeValue;
    }

    public void setPasswordOrWebAuthnFlow(boolean passwordOrWebAuthnFlow) {
        this.passwordOrWebAuthnFlow = passwordOrWebAuthnFlow;
    }

    public void setSteppedUp(StepUpStatus steppedUp) {
        this.steppedUp = steppedUp;
    }

    public void setLoginStatus(LoginStatus loginStatus) {
        this.loginStatus = loginStatus;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
