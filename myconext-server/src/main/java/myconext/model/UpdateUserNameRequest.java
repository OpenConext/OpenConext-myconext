package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateUserNameRequest implements Serializable {

    private String chosenName;

    private String givenName;

    private String familyName;
}
