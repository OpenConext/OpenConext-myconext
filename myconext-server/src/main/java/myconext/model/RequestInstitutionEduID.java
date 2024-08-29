package myconext.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Document(collection = "request_institution_eduid")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class RequestInstitutionEduID implements Serializable {

    @Id
    private String id;

    private String hash;

    @Setter
    private String emailHash;

    private Date expiresIn;

    private Map<String, Object> userInfo;

    @Setter
    private CreateInstitutionEduID createInstitutionEduID;

    @Setter
    private LoginStatus loginStatus;

    @Setter
    private String userId;

    public RequestInstitutionEduID(String hash, Map<String, Object> userInfo) {
        this.expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        this.hash = hash;
        this.userInfo = userInfo;
        this.loginStatus = LoginStatus.NOT_LOGGED_IN;
    }

}
