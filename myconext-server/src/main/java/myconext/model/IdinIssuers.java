package myconext.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IdinIssuers {

    private String country;
    private List<Issuer> issuers;
}
