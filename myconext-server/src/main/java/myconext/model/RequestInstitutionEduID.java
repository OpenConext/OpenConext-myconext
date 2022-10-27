package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@NoArgsConstructor
@Getter
@Document(collection = "request_institution_eduid")
public class RequestInstitutionEduID implements Serializable {

    @Id
    private String id;

    private String hash;

    private Date expiresIn;

    private Map<String, Object> userInfo;

    private CreateInstitutionEduID createInstitutionEduID;

    private LoginStatus loginStatus;

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
}
