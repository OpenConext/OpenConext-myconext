package surfid.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateUserRequest {

    private User user;
    private boolean updatePassword;
    private boolean clearPassword;
    private String currentPassword;
}
