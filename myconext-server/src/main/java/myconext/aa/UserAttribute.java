package myconext.aa;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserAttribute {

    private String name;
    private List<String> values;
}
