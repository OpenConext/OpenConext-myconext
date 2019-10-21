package surfid.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "authentication_requests")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {

    @Id
    private String id;

    private Date expiresIn;

    private String originalRequestUrl;
}
