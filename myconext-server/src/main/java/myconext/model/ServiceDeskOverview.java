package myconext.model;

import java.util.List;

/**
 * Statistics of the eduID accounts with a linked account for the institution of a service desk member
 *
 * @param accounts the number of eduID accounts per week, in chronological order
 * @param services the services the eduID accounts are logged into, most used first
 */
public record ServiceDeskOverview(List<AccountsPerWeek> accounts,
                                  List<ServiceUsage> services) {

    /**
     * @param week  ISO week, e.g. 2026-W45
     * @param added the number of accounts created in this week
     * @param total the number of accounts at the end of this week
     */
    public record AccountsPerWeek(String week, long added, long total) {
    }

    /**
     * @param name  the name of the service
     * @param users the number of eduID accounts that are used to login at the service
     */
    public record ServiceUsage(String name, long users) {
    }

}
