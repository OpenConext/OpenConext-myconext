package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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

    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date sendAt;

    public EmailsSend(String email) {
        this.email = email;
        this.sendAt = new Date();
    }
}
