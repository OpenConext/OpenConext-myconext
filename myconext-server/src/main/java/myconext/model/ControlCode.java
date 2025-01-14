package myconext.model;

import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ControlCode implements Serializable {

    private String firstName;
    private String lastName;
    private String dayOfBirth;
    @Setter
    private String code;

}
