package myconext.model;

import java.util.Date;

public interface ProvisionedLinkedAccount {

    String getGivenName();

    String getFamilyName();

    Date getCreatedAt();

    boolean isExternal();

    boolean isPreferred();

    void setPreferred(boolean preferred);
}
