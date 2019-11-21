package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateUserSecurityRequest {

    private String userId;
    private boolean updatePassword;
    private boolean clearPassword;
    private String currentPassword;
    private String newPassword;
}
