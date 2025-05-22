package myconext.api;

import myconext.model.SamlAuthenticationRequest;

@FunctionalInterface
public interface SamlAuthenticationCallBack {

    void apply(SamlAuthenticationRequest samlAuthenticationRequest);
}
