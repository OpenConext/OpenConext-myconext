package myconext.aa;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class UserAttribute {

    private String name;
    private List<String> values;

    public UserAttribute(String name, String value) {
        this.name = name;
        this.values = Collections.singletonList(value);
    }
}
