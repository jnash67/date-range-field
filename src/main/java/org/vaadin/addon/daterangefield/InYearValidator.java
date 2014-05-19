package org.vaadin.addon.daterangefield;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.ui.Field;

import java.util.Date;

public class InYearValidator extends AbstractValidator<Date> {

    Field yearField;
    DateRangeField drf;

    public InYearValidator(String message, Field<?> yearField, DateRangeField drf) {
        super(message);
        this.yearField = yearField;
        this.drf = drf;
    }

    @Override
    protected boolean isValidValue(Date value) {
        if (!drf.getForceRangeToBeWithinACalendarYear()) {
            // we're not forcing it within a year in which case this is always a valid value
            return true;
        }
        int year = (Integer) yearField.getValue();
        return DateUtil.isInYear(value, year);
    }

    @Override
    public Class<Date> getType() {
        return Date.class;
    }
}