package myconext.api;

import io.restassured.response.ValidatableResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MagicLinkResponse {

    public String authenticationRequestId;
    public ValidatableResponse response;

}
