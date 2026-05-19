package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RemoteProvider implements Serializable {

    private String entityId;
    private String name;
    private String nameNl;
    private String institutionGuid;
    private String logoUrl;

    @JsonIgnore
    public String nameByLanguage(String language) {
        if ("en".equalsIgnoreCase(language)) {
            return StringUtils.hasText(name) ? name : nameNl;
        }
        return StringUtils.hasText(nameNl) ? nameNl : name;
    }

}
