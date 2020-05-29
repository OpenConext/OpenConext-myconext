package myconext.aa;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserAttribute {

    private String name;
    private List<String> values;

    public UserAttribute(String name, String value) {
        this.name = name;
        this.values = Collections.singletonList(value);
    }
}
