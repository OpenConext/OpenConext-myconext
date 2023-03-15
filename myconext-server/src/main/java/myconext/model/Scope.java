package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Scope implements Serializable {

    private String name;
    private Map<String, String> descriptions;

}
