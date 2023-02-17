package myconext.tiqr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class StartAuthentication implements Serializable {

    private String sessionKey;
    private String url;
    private String qr;
    private boolean tiqrCookiePresent;

}
