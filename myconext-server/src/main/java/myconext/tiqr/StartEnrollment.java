package myconext.tiqr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class StartEnrollment implements Serializable {

    private String enrollmentKey;
    private String url;
    private String qrcode;

}
