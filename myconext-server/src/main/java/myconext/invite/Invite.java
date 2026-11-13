package myconext.invite;

import java.util.List;
import java.util.Map;

/**
 * Remote Invite application, the source of the roles of eduID users within an organization
 */
public interface Invite {

    /**
     * @return the names of the roles of the guests of the organization, by eduperson_principal_name. Never null.
     */
    Map<String, List<String>> rolesByEduPersonPrincipalName(String organizationGUID);

}
