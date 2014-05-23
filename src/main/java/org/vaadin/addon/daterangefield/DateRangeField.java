package org.vaadin.addon.daterangefield;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.DefaultFieldGroupFieldFactory;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
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
    private final Property<Integer> yearProperty;

    private final DateStepper fromField;
    private final DateStepper toField;
    private final IntStepper yearField;

    private boolean withinYear;
    private boolean allowUserToChangeYear;
    private IntegerRangeValidator minYearValidator, maxYearValidator;
    private BeforeValidator maxYearFromDateValidator, maxYearToDateValidator;
    private AfterValidator minYearFromDateValidator, minYearToDateValidator;
    private InYearValidator fromDateInYearValidator, toDateInYearValidator;

    private HorizontalLayout dateLayout = new HorizontalLayout();
    private String errorStyleName = "";

    public DateRangeField(Property<Date> fromProperty, Property<Date> toProperty) {
        this(fromProperty, toProperty, true, (Integer) null, true);
    }

    public DateRangeField(Property<Date> fromProperty, Property<Date> toProperty,
                          boolean forceRangeToBeWithinACalendarYear, Property<Integer> yearProperty,
                          boolean allowUserToChangeYear) {
        this.fromProperty = fromProperty;
        this.toProperty = toProperty;
        this.yearProperty = yearProperty;
        this.withinYear = forceRangeToBeWithinACalendarYear;
        this.allowUserToChangeYear = allowUserToChangeYear;

        DateRange dr = new DateRange(fromProperty.getValue(), toProperty.getValue());
        setValue(dr);
        BeanItem<DateRange> dateRangeBean = new BeanItem<DateRange>(dr);

        FieldGroup fieldGroup = new FieldGroup(dateRangeBean);
        fieldGroup.setFieldFactory(new DateRangeFieldFactory());
        this.fromField = (DateStepper) fieldGroup.buildAndBind("From", "from");
        this.toField = (DateStepper) fieldGroup.buildAndBind("To", "to");
        addValidator(fromField, new BeforeValidator("Start date cannot be after the end date", toField, true));
        addValidator(toField, new AfterValidator("End date cannot be before the start date", fromField, true));
        this.yearField = new IntStepper("Year");
        this.yearField.setPropertyDataSource(yearProperty);
        if (null == yearProperty.getValue()) {
            this.yearField.setValue(DateUtil.currentYear());
        }
        setAllowUserToChangeYear(allowUserToChangeYear);
        setForceRangeToBeWithinACalendarYear(forceRangeToBeWithinACalendarYear);
        ValueChangeListener listener = new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                DateRange newValue = new DateRange(fromField.getValue(), toField.getValue());
                setValue(newValue);
            }
        };
        fromField.addValueChangeListener(listener);
        toField.addValueChangeListener(listener);
        // show validation errors on the individual fields, not on the entire field
        setValidationVisible(false);
    }

    public DateRangeField(Property<Date> fromProperty, Property<Date> toProperty,
                          boolean forceRangeToBeWithinACalendarYear, Integer whichYear, boolean allowUserToChangeYear) {
        this(fromProperty, toProperty, forceRangeToBeWithinACalendarYear, new ObjectProperty<Integer>(whichYear,
                Integer.class), allowUserToChangeYear);
    }

    public void setAllowUserToChangeYear(final boolean b) {
        this.allowUserToChangeYear = b;
        this.yearField.setEnabled(b);
    }

    public boolean getForceRangeToBeWithinACalendarYear() {
        return this.withinYear;
    }

    public void setForceRangeToBeWithinACalendarYear(final boolean b) {
        this.withinYear = b;
        this.yearField.setVisible(b);
        if (b) {
            fromDateInYearValidator = new InYearValidator("From date not in the specified year", yearField, this);
            addValidator(fromField, fromDateInYearValidator);
            toDateInYearValidator = new InYearValidator("To date not in the specified year", yearField, this);
            addValidator(toField, toDateInYearValidator);
        } else {
            removeAllValidators(yearField);
            removeValidator(fromField, fromDateInYearValidator);
            fromDateInYearValidator = null;
            removeValidator(toField, toDateInYearValidator);
            toDateInYearValidator = null;
        }
    }

    public void setRequiredCalendarYear(int year) {
        if (year < 1) {
            return;
        }
        this.yearField.setValue(year);
        setForceRangeToBeWithinACalendarYear(this.withinYear);
    }

    @Override
    public void commit() throws SourceException, Validator.InvalidValueException {
        super.commit();
        this.yearProperty.setValue(yearField.getValue());
        this.fromProperty.setValue(fromField.getValue());
        this.toProperty.setValue(toField.getValue());
    }

    @Override
    protected void validate(DateRange fieldValue) throws Validator.InvalidValueException {
        Validator.InvalidValueException first = highlightInvalidFields();
        if (null != first) {
            throw first;
        }
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
                yearChange(true);
            }
        });
        dateLayout.setSpacing(true);
        dateLayout.addComponent(yearField);
        dateLayout.addComponent(fromField);
        dateLayout.addComponent(toField);
        addErrorChangeListener(yearField);
        addErrorChangeListener(fromField);
        addErrorChangeListener(toField);
        dateLayout.setComponentAlignment(yearField, Alignment.BOTTOM_CENTER);
        dateLayout.setComponentAlignment(fromField, Alignment.BOTTOM_CENTER);
        dateLayout.setComponentAlignment(toField, Alignment.BOTTOM_CENTER);
        fromField.setWidth("90px");
        toField.setWidth("90px");
        return dateLayout;
    }

    public void setErrorStyleName(String styleName) {
        this.errorStyleName = styleName;
    }

    public String getErrorStyleName() {
        return this.errorStyleName;
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
        this.yearField.setLocale(locale);
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
        removeValidator(yearField, minYearValidator);
        minYearValidator = null;

        removeValidator(fromField, minYearFromDateValidator);
        minYearFromDateValidator = null;

        removeValidator(toField, minYearToDateValidator);
        minYearToDateValidator = null;

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
        minYearToDateValidator = new AfterValidator("To date must be in or after " + minYear,
                DateUtil.firstDayOfYear(minYear), true);
        addValidator(yearField, minYearValidator);
        addValidator(fromField, minYearFromDateValidator);
        addValidator(toField, minYearToDateValidator);
    }

    public int getMaxYear() {
        return maxYear;
    }

    public void setMaxYear(int year) {
        removeValidator(yearField, maxYearValidator);
        maxYearValidator = null;

        removeValidator(fromField, maxYearFromDateValidator);
        maxYearFromDateValidator = null;

        removeValidator(toField, maxYearToDateValidator);
        maxYearToDateValidator = null;

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
        maxYearToDateValidator = new BeforeValidator("To date must be in or before " + maxYear,
                DateUtil.firstDayOfYear(maxYear), true);
        addValidator(yearField, maxYearValidator);
        addValidator(fromField, maxYearFromDateValidator);
        addValidator(toField, maxYearToDateValidator);
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

    public void removeAllValidators(Field<?> f) {
        f.removeAllValidators();
        highlightInvalidField(f);
    }

    public void removeValidator(Field<?> f, Validator toRemove) {
        f.removeValidator(toRemove);
        highlightInvalidField(f);
    }

    public void addValidator(Field<?> f, Validator toAdd) {
        f.addValidator(toAdd);
        highlightInvalidField(f);
    }

    // this enables an initial validation before the fields have changed
    public Validator.InvalidValueException highlightInvalidField(Field<?> f) {
        Validator.InvalidValueException err = null;
        try {
            f.validate();
            f.removeStyleName(getErrorStyleName());
        } catch (Validator.InvalidValueException ive) {
            err = ive;
            f.addStyleName(getErrorStyleName());
        }
        return err;
    }

    public void addErrorChangeListener(final Field<?> f) {
        f.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                //noinspection ThrowableResultOfMethodCallIgnored
                highlightInvalidFields();
            }
        });
    }

    public Validator.InvalidValueException highlightInvalidFields() {
        Validator.InvalidValueException err1 = highlightInvalidField(this.fromField);
        Validator.InvalidValueException err2 = highlightInvalidField(this.toField);
        Validator.InvalidValueException err3 = highlightInvalidField(this.yearField);
        if (null != err1) {
            return err1;
        }
        if (null != err2) {
            return err2;
        }
        if (null != err3) {
            return err3;
        }
        return null;
    }
}
