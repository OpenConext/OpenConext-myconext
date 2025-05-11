package myconext.api;

import io.restassured.response.ValidatableResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClientAuthenticationResponse {

    public String authenticationRequestId;
    public ValidatableResponse response;

}
