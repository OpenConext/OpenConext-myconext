package myconext.tiqr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PhoneVerification implements Serializable {

    @NotNull
    private String phoneVerification;

}
