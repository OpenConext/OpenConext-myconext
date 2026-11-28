package myconext.model;

import java.util.List;

/**
 * Statistics of the eduID accounts with a linked account for the institution of a service desk member. Kept compact,
 * as the labels and running totals of the accounts per month are derived by the client.
 *
 * @param firstMonth the month of the first entry in added, e.g. 2026-03, null if there are no accounts with a creation date
 * @param baseline   the number of accounts without a creation date, which are part of the total from the start
 * @param added      the number of accounts created per month, starting at firstMonth and ending at the current month
 */
public record ServiceDeskStats(String firstMonth,
                               long baseline,
                               List<Long> added) {
}
