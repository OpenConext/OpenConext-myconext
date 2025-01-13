package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ControlCode implements Serializable {

    private String firstName;
    private String lastName;
    private String dayOfBirth;
    @Setter
    private String code;

}
