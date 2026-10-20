package myconext.model;

/**
 * The user actions which are tracked as {@link Activity} in the user document and shown in the
 * 'recent activity' overview of the user's home page.
 */
public enum ActivityEvent {

    ACCOUNT_CREATED,
    NAME_UPDATED,
    LANGUAGE_CHANGED,
    EMAIL_CHANGED,
    PASSWORD_ADDED,
    PASSWORD_UPDATED,
    PASSWORD_DELETED,
    PASSKEY_ADDED,
    PASSKEY_RENAMED,
    PASSKEY_DELETED,
    APP_ACTIVATED,
    APP_DEACTIVATED,
    RECOVERY_METHOD_CHANGED,
    INSTITUTION_LINKED,
    INSTITUTION_REMOVED,
    PREFERRED_INSTITUTION_CHANGED,
    IDENTITY_VERIFIED,
    IDENTITY_REMOVED,
    CONTROL_CODE_CREATED,
    CONTROL_CODE_DELETED,
    SERVICE_REMOVED,
    TOKENS_REMOVED

}
