package myconext.model;

public enum UserInactivity {

    //1 year (= 4 year before the 5-year mark)
    YEAR_1_INTERVAL(4L * 365, 365L),
    //3 year (= 3 year before the 5-year mark)
    YEAR_3_INTERVAL(3L * 365, 2L * 365),
    //
    MONTH_1_BEFORE_5_YEARS(30L, (4L * 365) + (11L * 30)),
    WEEK_1_BEFORE_5_YEARS(7L, (4L * 365) + (51L * 7));

    private final long beforeDays;
    private final long afterDays;


    UserInactivity(long beforeDays, long afterDays) {
        this.afterDays = afterDays;
        this.beforeDays = beforeDays;
    }

    }
