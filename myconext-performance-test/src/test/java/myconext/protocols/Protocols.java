package myconext.protocols;

import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.http.HttpDsl.http;

public class Protocols {
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8081");
    private static final String USERNAME = System.getProperty("username", "studielink");
    private static final String PASSWORD = System.getProperty("password", "secret");

    public static HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .basicAuth(USERNAME, PASSWORD);
}
