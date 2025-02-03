package myconext.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@ToString
public class ControlCode implements Serializable {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    @Setter
    private String dayOfBirth;
    @Setter
    @Indexed
    private String code;
    @Setter
    private String documentId;
    @Setter
    private long createdAt;

    @Setter
    private String userUid;

    public ControlCode(String firstName, String lastName, String dayOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dayOfBirth = dayOfBirth;
    }
}
