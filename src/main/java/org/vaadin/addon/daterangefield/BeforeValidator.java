package org.vaadin.addon.daterangefield;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.ui.Field;

import java.util.Date;

public class BeforeValidator extends AbstractValidator<Date> {
    boolean compareToField;
    boolean allowOnTheDate;
    Field otherField;
    Date constantField;

    public BeforeValidator(String message, Field<?> otherDateFieldWeMustBeBefore, boolean allowOnTheDate) {
        super(message);
        this.otherField = otherDateFieldWeMustBeBefore;
        compareToField = true;
        this.allowOnTheDate = allowOnTheDate;
    }

    @SuppressWarnings("UnusedDeclaration")
    public BeforeValidator(String message, Date constantDateWeMustBeBefore, boolean allowOnTheDate) {
        super(message);
        this.constantField = constantDateWeMustBeBefore;
        compareToField = false;
        this.allowOnTheDate = allowOnTheDate;
    }

    @Override
    protected boolean isValidValue(Date value) {
        if (compareToField) {
            if (allowOnTheDate) {
                return DateUtil.isOnOrBefore(value, (Date) otherField.getValue());
            }   else {
                return DateUtil.isBefore(value, (Date) otherField.getValue());
            }
        } else {
            if (allowOnTheDate) {
                return DateUtil.isOnOrBefore(value, constantField);
            }   else {
                return DateUtil.isBefore(value, constantField);
            }
        }
    }

    @Override
    public Class<Date> getType() {
        return Date.class;
    }
}
