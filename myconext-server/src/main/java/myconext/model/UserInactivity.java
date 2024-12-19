package myconext.model;

import lombok.Getter;

@Getter
public enum UserInactivity {

    // 1 year (= 4 year before the 5-year mark)
    YEAR_1_INTERVAL(365L,
            null,
            "1 year",
            "1 jaar",
            "4 years",
            "4 jaren"),
    // 3 year (= 3 year before the 5-year mark)
    YEAR_3_INTERVAL(2L * 365,
            YEAR_1_INTERVAL,
            "2 years",
            "2 jaren",
            "3 years",
            "3 jaren"),
    // 1 month before the 5-year mark
    MONTH_1_BEFORE_5_YEARS((4L * 365) + (11L * 30),
            YEAR_3_INTERVAL,
            "almost 5 years",
            "bijna 5 jaar",
            "1 month",
            "1 maand"),
    // 1 week before the-5 year mark
    WEEK_1_BEFORE_5_YEARS((4L * 365) + (51L * 7),
            MONTH_1_BEFORE_5_YEARS,
            "almost 5 years",
            "bijna 5 jaar",
            "1 week",
            "1 week");

    private final long inactivityDays;
    private final UserInactivity previousUserInactivity;
    private final String inactivityPeriodEn;
    private final String inactivityPeriodNl;
    private final String deletionPeriodEn;
    private final String deletionPeriodNl;

    UserInactivity(long inactivityDays,
                   UserInactivity previousUserInactivity,
                   String inactivityPeriodEn,
                   String inactivityPeriodNl,
                   String deletionPeriodEn,
                   String deletionPeriodNl) {
        this.inactivityDays = inactivityDays;
        this.previousUserInactivity = previousUserInactivity;
        this.inactivityPeriodEn = inactivityPeriodEn;
        this.inactivityPeriodNl = inactivityPeriodNl;
        this.deletionPeriodEn = deletionPeriodEn;
        this.deletionPeriodNl = deletionPeriodNl;
    }

}
