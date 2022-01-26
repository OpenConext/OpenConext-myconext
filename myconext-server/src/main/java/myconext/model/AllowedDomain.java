package myconext.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AllowedDomain implements Serializable {

    @EqualsAndHashCode.Include
    private String emailDomain;

    private String schacHomeOrganization;

    public AllowedDomain toLowerCase() {
        this.emailDomain = StringUtils.hasText(this.emailDomain) ? emailDomain.toLowerCase() : emailDomain;
        this.schacHomeOrganization = StringUtils.hasText(this.schacHomeOrganization) ? schacHomeOrganization.toLowerCase() : schacHomeOrganization;
        return this;
    }

}
