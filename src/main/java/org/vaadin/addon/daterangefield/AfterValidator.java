package org.vaadin.addon.daterangefield;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.ui.Field;

import java.util.Date;

public class AfterValidator extends AbstractValidator<Date> {
    boolean compareToField;
    boolean allowOnTheDate;
    Field otherField;
    Date constantField;

    public AfterValidator(String message, Field<?> otherDateFieldWeMustBeAfter, boolean allowOnTheDate) {
        super(message);
        this.otherField = otherDateFieldWeMustBeAfter;
        compareToField = true;
        this.allowOnTheDate = allowOnTheDate;
    }

    public AfterValidator(String message, Date otherDateFieldWeMustBeAfter, boolean allowOnTheDate) {
        super(message);
        this.constantField = otherDateFieldWeMustBeAfter;
        compareToField = false;
        this.allowOnTheDate = allowOnTheDate;
    }

    @Override
    protected boolean isValidValue(Date value) {
        if (compareToField) {
            if (allowOnTheDate) {
                return DateUtil.isOnOrAfter(value, (Date) otherField.getValue());
            } else {
                return DateUtil.isAfter(value, (Date) otherField.getValue());
            }
        } else {
            if (allowOnTheDate) {
                return DateUtil.isOnOrAfter(value, constantField);
            } else {
                return DateUtil.isAfter(value, constantField);
            }
        }
    }

    @Override
    public Class<Date> getType() {
        return Date.class;
    }
}
