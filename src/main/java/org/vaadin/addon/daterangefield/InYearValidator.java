package org.vaadin.addon.daterangefield;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.ui.Field;

import java.util.Date;

public class InYearValidator extends AbstractValidator<Date> {

    Field yearField;

    public InYearValidator(String message, Field<?> yearField) {
        super(message);
        this.yearField = yearField;
    }

    @Override
    protected boolean isValidValue(Date value) {
        int year = (Integer) yearField.getValue();
        if (year < 1) {
            // we're not forcing it within a year in which case this is always a valid value
            return true;
        }
        return DateUtil.isInYear(value, year);
    }

    @Override
    public Class<Date> getType() {
        return Date.class;
    }
}