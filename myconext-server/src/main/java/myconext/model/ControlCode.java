package myconext.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.time.Instant;

@Getter
@NoArgsConstructor
@ToString
public class ControlCode implements Serializable {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String dayOfBirth;
    @Setter
    @Indexed
    private String code;
    @Setter
    private long createdAt;

    public ControlCode(String firstName, String lastName, String dayOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dayOfBirth = dayOfBirth;
    }
}
