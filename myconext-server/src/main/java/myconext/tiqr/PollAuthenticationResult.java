package myconext.tiqr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PollAuthenticationResult implements Serializable {

    private String status;
    private String redirect;
    private String hash;
    private long suspendedUntil;
}
