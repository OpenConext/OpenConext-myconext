package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateUserSecurityRequest implements Serializable {

    private String userId;
    private String currentPassword;
    private String newPassword;
    private String hash;
}
