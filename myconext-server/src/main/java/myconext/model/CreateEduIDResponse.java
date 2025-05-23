package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEduIDResponse implements Serializable {

    private int status;

    private String hash;

}
