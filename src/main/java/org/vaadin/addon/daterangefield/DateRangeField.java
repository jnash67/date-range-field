package org.vaadin.addon.daterangefield;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.DefaultFieldGroupFieldFactory;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import org.vaadin.risto.stepper.DateStepper;
import org.vaadin.risto.stepper.IntStepper;

import java.util.Date;
import java.util.Locale;

@SuppressWarnings("unused")
public class DateRangeField extends CustomField<DateRange> {

    public class DateRangeFieldFactory extends DefaultFieldGroupFieldFactory {
        public DateRangeFieldFactory() {
            super();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Field> T createField(Class<?> dataType, Class<T> fieldType) {
            if (Date.class.isAssignableFrom(dataType)) {
                DateStepper field = new DateStepper();
                field.setMouseWheelEnabled(true);
                field.setImmediate(true);
                // locale must be set or it craps out
                field.setLocale(Locale.US);
                return (T) field;
            }
            return super.createField(dataType, fieldType);
        }
    }

    protected int minYear = -1;
    protected int maxYear = -1;

    private final Property<Date> fromProperty;
    private final Property<Date> toProperty;

    private final DateStepper fromField;
    private final DateStepper toField;
    private final IntStepper yearField;

    private boolean withinYear;
    private boolean allowUserToChangeYear;
    private IntegerRangeValidator minYearValidator, maxYearValidator;
    private BeforeValidator maxYearFromDateValidator, maxYearToDateValidator;
    private AfterValidator minYearFromDateValidator, minYearToDateValidator;
    private int initialYear;

    public DateRangeField(Property<Date> fromProperty, Property<Date> toProperty) {
        this(fromProperty, toProperty, true, null, true);
    }

    public DateRangeField(Property<Date> fromProperty, Property<Date> toProperty,
                          boolean forceRangeToBeWithinACalendarYear, Integer whichYear, boolean allowUserToChangeYear) {
        this.fromProperty = fromProperty;
        this.toProperty = toProperty;
        this.withinYear = forceRangeToBeWithinACalendarYear;
        if (null == whichYear) {
            this.initialYear = DateUtil.currentYear();
        } else {
            this.initialYear = whichYear;
        }
        this.allowUserToChangeYear = allowUserToChangeYear;

        DateRange dr = new DateRange(fromProperty.getValue(), toProperty.getValue());
        BeanItem<DateRange> dateRangeBean = new BeanItem<DateRange>(dr);

        FieldGroup fieldGroup = new FieldGroup(dateRangeBean);
        fieldGroup.setFieldFactory(new DateRangeFieldFactory());
        this.fromField = (DateStepper) fieldGroup.buildAndBind("From", "from");
        this.toField = (DateStepper) fieldGroup.buildAndBind("To", "to");
        // no need to add a corresponding validator to the toField.  If start date is after the end
        // date, then the end date is before the start date.
        fromField.addValidator(new BeforeValidator("Start date cannot be after the end date", toField, true));
        this.yearField = new IntStepper("Year");
        fromField.addValidator(new InYearValidator("From date not in the specified year", yearField));
        toField.addValidator(new InYearValidator("To date not in the specified year", yearField));
        setAllowUserToChangeYear(allowUserToChangeYear);
        setForceRangeToBeWithinACalendarYear(forceRangeToBeWithinACalendarYear);
    }

    public void setAllowUserToChangeYear(final boolean b) {
        this.allowUserToChangeYear = b;
        this.yearField.setEnabled(b);
    }

    public void setForceRangeToBeWithinACalendarYear(final boolean b) {
        this.withinYear = b;
        this.yearField.setVisible(b);
        if (b) {
            this.yearField.setValue(initialYear);
            if (this.minYear > 0) {
                setMinYear(this.minYear);
            }
            if (this.maxYear > 0) {
                setMaxYear(this.minYear);
            }
        } else {
            this.yearField.removeAllValidators();
            this.yearField.setValue(-1);
        }
    }

    public void setRequiredCalendarYear(int year) {
        if (year < 1) {
            return;
        }
        this.initialYear = year;
        setForceRangeToBeWithinACalendarYear(this.withinYear);
    }

    @Override
    public void commit() throws SourceException, Validator.InvalidValueException {
        super.commit();
        this.yearField.validate();
        this.fromProperty.setValue(fromField.getValue());
        this.toProperty.setValue(toField.getValue());
    }

    @Override
    protected void validate(DateRange fieldValue) throws Validator.InvalidValueException {
        this.fromField.validate();
        this.toField.validate();
        this.yearField.validate();
    }

    @Override
    protected Component initContent() {
        fromField.setInvalidAllowed(true);
        fromField.setManualInputAllowed(true);
        fromField.setImmediate(true);
        toField.setInvalidAllowed(true);
        toField.setManualInputAllowed(true);
        toField.setImmediate(true);

        yearField.setWidth("65px");
        yearField.setMouseWheelEnabled(true);
        yearField.setImmediate(true);
        yearField.setInvalidAllowed(false);
        yearField.setManualInputAllowed(false);
        yearField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (-1 == yearField.getValue()) {
                    return;
                }
                yearChange(true);
            }
        });
        HorizontalLayout dateLayout = new HorizontalLayout();
        dateLayout.setSpacing(true);
        dateLayout.addComponent(yearField);
        dateLayout.addComponent(fromField);
        dateLayout.addComponent(toField);
        dateLayout.setComponentAlignment(yearField, Alignment.BOTTOM_CENTER);
        dateLayout.setComponentAlignment(fromField, Alignment.BOTTOM_CENTER);
        dateLayout.setComponentAlignment(toField, Alignment.BOTTOM_CENTER);
        fromField.setWidth("90px");
        toField.setWidth("90px");
        return dateLayout;
    }

    private void yearChange(boolean keepDates) {
        int y = yearField.getValue();
        Date firstDayOfNewYear = DateUtil.firstDayOfYear(y);
        Date lastDayOfNewYear = DateUtil.lastDayOfYear(y);
        Date start = fromField.getValue();
        Date end = toField.getValue();
        if (null == start) {
            start = firstDayOfNewYear;
            fromField.setValue(start);
        } else {
            if (!DateUtil.isInYear(start, y)) {
                if (keepDates) {
                    start = DateUtil.sameDayDifferentYear(start, y);
                } else {
                    start = firstDayOfNewYear;
                }
                fromField.setValue(start);
            }
        }
        if (null == end) {
            end = lastDayOfNewYear;
            toField.setValue(end);
        } else {
            if (!DateUtil.isInYear(end, y)) {
                if (keepDates) {
                    end = DateUtil.sameDayDifferentYear(end, y);
                } else {
                    end = lastDayOfNewYear;
                }
                toField.setValue(end);
            }
        }
        fromField.setMinValue(firstDayOfNewYear);
        fromField.setMaxValue(lastDayOfNewYear);
        toField.setMinValue(firstDayOfNewYear);
        toField.setMaxValue(lastDayOfNewYear);
    }

    @Override
    public Class<? extends DateRange> getType() {
        return DateRange.class;
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        this.fromField.setLocale(locale);
        this.toField.setLocale(locale);
        if (null != this.yearField) {
            this.yearField.setLocale(locale);
        }
    }


    public void setFromDateCaption(String caption) {
        this.fromField.setCaption(caption);
    }

    public void setToDateCaption(String caption) {
        this.toField.setCaption(caption);
    }

    public void setYearFieldCaption(String caption) {
        this.yearField.setCaption(caption);
    }

    public int getMinYear() {
        return minYear;
    }

    public void setMinYear(int year) {
        if (null != minYearValidator) {
            yearField.removeValidator(minYearValidator);
            minYearValidator = null;
        }
        if (null != minYearFromDateValidator) {
            fromField.removeValidator(minYearFromDateValidator);
            minYearFromDateValidator = null;
        }
        if (null != minYearToDateValidator) {
            toField.removeValidator(minYearToDateValidator);
            minYearToDateValidator = null;
        }
        if (year < 1) {
            this.minYear = -1;
            return;
        }
        this.minYear = year;
        if (this.maxYear > 0) {
            if (this.minYear > this.maxYear) {
                setMaxYear(this.minYear);
            }
        }
        yearField.setMinValue(minYear);
        minYearValidator = new IntegerRangeValidator("Year must be later or equal to " + minYear, minYear, null);
        minYearFromDateValidator = new AfterValidator("From date must be in or after " + minYear,
                DateUtil.firstDayOfYear(minYear), true);
        minYearToDateValidator = new AfterValidator("From date must be in or after " + minYear,
                DateUtil.firstDayOfYear(minYear), true);
        yearField.addValidator(minYearValidator);
        fromField.addValidator(minYearFromDateValidator);
        toField.addValidator(minYearToDateValidator);
    }

    public int getMaxYear() {
        return maxYear;
    }

    public void setMaxYear(int year) {
        if (null != maxYearValidator) {
            yearField.removeValidator(maxYearValidator);
            maxYearValidator = null;
        }
        if (null != maxYearFromDateValidator) {
            fromField.removeValidator(maxYearFromDateValidator);
            maxYearFromDateValidator = null;
        }
        if (null != maxYearToDateValidator) {
            toField.removeValidator(maxYearToDateValidator);
            maxYearToDateValidator = null;
        }
        if (year < 1) {
            this.maxYear = -1;
            return;
        }
        this.maxYear = year;
        if (this.minYear > 0) {
            if (this.maxYear < this.minYear) {
                setMinYear(this.maxYear);
            }
        }
        yearField.setMaxValue(maxYear);
        maxYearValidator = new IntegerRangeValidator("Year must be earlier or equal to " + maxYear, null, maxYear);
        maxYearFromDateValidator = new BeforeValidator("From date must be in or before " + maxYear,
                DateUtil.firstDayOfYear(maxYear), true);
        maxYearToDateValidator = new BeforeValidator("From date must be in or before " + maxYear,
                DateUtil.firstDayOfYear(maxYear), true);
        yearField.addValidator(maxYearValidator);
        fromField.addValidator(maxYearFromDateValidator);
        toField.addValidator(maxYearToDateValidator);
    }

    public DateStepper getFromField() {
        return fromField;
    }

    public DateStepper getToField() {
        return toField;
    }

    public IntStepper getYearField() {
        return yearField;
    }

    @Override
    protected boolean isEmpty() {
        return ((null == fromField.getValue()) || (null == toField.getValue()));
    }

    @Override
    public Property getPropertyDataSource() {
        throw new UnsupportedOperationException("The two properties passed in the constructor are the datasource.");
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        throw new UnsupportedOperationException("The two properties passed in the constructor are the datasource.");
    }

    @Override
    public void setConverter(Class<?> datamodelType) {
        throw new UnsupportedOperationException("The display is hardwired in.");
    }
    @Override
    protected void setInternalValue(DateRange newValue) {
        throw new UnsupportedOperationException("The 'internal value' is in the fields.");
    }

    @Override
    protected DateRange getInternalValue() {
        return null;
    }

}
