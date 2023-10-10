package myconext.cron;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class IdentityProvider implements Serializable {

    private String displayNameEn;
    private String displayNameNl;
    private String logoUrl;
}
