package myconext.model;

import org.springframework.util.StringUtils;

import java.util.Date;

public interface ProvisionedLinkedAccount {

    String getGivenName();

    String getFamilyName();

    Date getCreatedAt();

    boolean isExternal();

    boolean isPreferred();

    void setPreferred(boolean preferred);
}
