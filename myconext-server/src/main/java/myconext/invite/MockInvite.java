package myconext.invite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockInvite implements Invite {

    private final Map<String, Map<String, List<String>>> roles = new HashMap<>();

    @Override
    public Map<String, List<String>> rolesByEduPersonPrincipalName(String organizationGUID) {
        return roles.getOrDefault(organizationGUID, Map.of());
    }

    public void setRoles(String organizationGUID, Map<String, List<String>> rolesByEppn) {
        roles.put(organizationGUID, rolesByEppn);
    }

    public void clear() {
        roles.clear();
    }

}
