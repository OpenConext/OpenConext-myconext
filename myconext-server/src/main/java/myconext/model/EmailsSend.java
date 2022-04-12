package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@Getter
@Document(collection = "emails_send")
public class EmailsSend implements Serializable {

    @Id
    private String id;

    private String email;

    private Date sendAt;

    public EmailsSend(String email) {
        this.email = email;
        this.sendAt = new Date();
    }
}
