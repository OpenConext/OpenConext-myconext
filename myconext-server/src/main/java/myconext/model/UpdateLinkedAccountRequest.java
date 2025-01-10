package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateLinkedAccountRequest implements Serializable {

    private String eduPersonPrincipalName;
    private String subjectId;
    private boolean external;
    private String idpScoping;
    private String schacHomeOrganization;


}
