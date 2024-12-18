package myconext.model;

import lombok.Getter;

@Getter
public enum UserInactivity {

    // 1 year (= 4 year before the 5-year mark)
    YEAR_1_INTERVAL(365L),
    // 3 year (= 3 year before the 5-year mark)
    YEAR_3_INTERVAL(2L * 365),
    // 1 month before the 5-year mark
    MONTH_1_BEFORE_5_YEARS((4L * 365) + (11L * 30)),
    // 1 week before the-5 year mark
    WEEK_1_BEFORE_5_YEARS((4L * 365) + (51L * 7));

    private final long inactivityDays;

    UserInactivity(long inactivityDays) {
        this.inactivityDays = inactivityDays;
    }

}
