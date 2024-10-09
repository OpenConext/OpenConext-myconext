package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Indexed
    private String hash;

    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date expiresIn;

    private String userId;

    private String requesterEntityId;

    private boolean accountLinkingRequired;

    private boolean mfaProfileRequired;

    private List<String> authenticationContextClassReferences;

    private boolean passwordOrWebAuthnFlow;

    private boolean tiqrFlow;

    private boolean rememberMe;

    private String rememberMeValue;

    private StepUpStatus steppedUp = StepUpStatus.NONE;

    private boolean testInstance;

    private LoginStatus loginStatus = LoginStatus.NOT_LOGGED_IN;

    private String verificationCode;

    private int retryVerificationCode;

    private String serviceName;

    private boolean rememberMeQuestionAsked = false;

    public SamlAuthenticationRequest(boolean testInstance) {
        this.id = UUID.randomUUID().toString();
        this.testInstance = testInstance;
        this.requesterEntityId = "test";
        this.authenticationContextClassReferences = new ArrayList<>();
        this.expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

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
        this.expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        this.requesterEntityId = requesterEntityId;
        this.accountLinkingRequired = accountLinkingRequired;
        this.mfaProfileRequired = mfaProfileRequired;
        this.authenticationContextClassReferences = authenticationContextClassReferences;
        invariant();
    }

    @Transient
    private void invariant() {
        if ((this.isAccountLinkingRequired() || this.isMfaProfileRequired()) &&
                CollectionUtils.isEmpty(this.authenticationContextClassReferences)) {
            throw new IllegalArgumentException("authenticationContextClassReference is required when account linking or mfa profiles is required");
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

    public void setRetryVerificationCode(int retryVerificationCode) {
        this.retryVerificationCode = retryVerificationCode;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setRememberMeQuestionAsked(boolean rememberMeQuestionAsked) {
        this.rememberMeQuestionAsked = rememberMeQuestionAsked;
    }

    public void setTiqrFlow(boolean tiqrFlow) {
        this.tiqrFlow = tiqrFlow;
    }
}
