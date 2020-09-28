package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateUserSecurityRequest {

    private String userId;
    private String currentPassword;
    private String newPassword;
    private String hash;
}
