package org.vaadin.addon.daterangefield;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * This represents an inclusive date range.  The from date is in the range.  The to date is in the range.  For
 * non-inclusive, you have to increment the from date by one and decrement the end date by one.
 */
@Data
// two ranges are equal if the from and to are equal
@EqualsAndHashCode()
public class DateRange {
    private Date from;
    private Date to;

    public DateRange(Date from, Date to) {
        this.from = from;
        this.to = to;
    }
}
