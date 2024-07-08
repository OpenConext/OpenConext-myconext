package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class RequestInstitutionEduID implements Serializable {

    @Id
    private String id;

    private String hash;

    private String emailHash;

    private Date expiresIn;

    private Map<String, Object> userInfo;

    private CreateInstitutionEduID createInstitutionEduID;

    private LoginStatus loginStatus;

    private String userId;

    public RequestInstitutionEduID(String hash, Map<String, Object> userInfo) {
        this.expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        this.hash = hash;
        this.userInfo = userInfo;
        this.loginStatus = LoginStatus.NOT_LOGGED_IN;
    }

    public void setCreateInstitutionEduID(CreateInstitutionEduID createInstitutionEduID) {
        this.createInstitutionEduID = createInstitutionEduID;
    }

    public void setLoginStatus(LoginStatus loginStatus) {
        this.loginStatus = loginStatus;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmailHash(String emailHash) {
        this.emailHash = emailHash;
    }
}
