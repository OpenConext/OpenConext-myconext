package myconext.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@ToString(exclude = {"logo"})
@AllArgsConstructor
@NoArgsConstructor
public class VerifyIssuer implements Serializable {

    private String id;
    private String name;
    private String logo;

}
