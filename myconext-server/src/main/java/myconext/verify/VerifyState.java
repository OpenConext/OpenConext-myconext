package myconext.verify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import myconext.model.IdpScoping;
import myconext.model.VerifyIssuer;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class VerifyState implements Serializable {

    private final String stateIdentifier;
    private final IdpScoping idpScoping;
    private final VerifyIssuer verifyIssuer;

}
