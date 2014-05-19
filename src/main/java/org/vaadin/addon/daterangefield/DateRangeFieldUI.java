package org.vaadin.addon.daterangefield;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import java.util.Locale;

public class DateRangeFieldUI extends UI {

    DateRangeField drf;
    CheckBox forceRangeToBeWithinACalendarYear;
    TextField whatYear, minYear, maxYear;
    CheckBox allowUserToChangeYear;

    @Override
    protected void init(final VaadinRequest request) {
        getPage().setTitle("DateRangeField Demo");

        // only using DateField because it's an easy way to create a date Property
        DateField df1 = new DateField(null, DateUtil.firstDayOfYear(DateUtil.currentYear()));
        DateField df2 = new DateField(null, DateUtil.lastDayOfYear(DateUtil.currentYear()));
        drf = new DateRangeField(df1, df2, true, (Integer) null, false);
        Button submit = new Button("Submit");
        submit.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    drf.commit();
                } catch (Validator.InvalidValueException ive) {
                    Notification.show(ive.getMessage(), Notification.Type.WARNING_MESSAGE);
                    return;
                }
                Notification.show("Good date range");
            }
        });

        // create the layout with the export options
        final VerticalLayout options = new VerticalLayout();
        options.setSpacing(true);
        final Label headerLabel = new Label("<b>DateRangeField Options</b>", ContentMode.HTML);
        final Label verticalSpacer = new Label();
        verticalSpacer.setHeight("10px");

        forceRangeToBeWithinACalendarYear = new CheckBox("Force Range to Be Within a Calendar Year", true);
        forceRangeToBeWithinACalendarYear.setImmediate(true);

        Integer currentYear = DateUtil.currentYear();
        whatYear = new TextField("What Year", currentYear.toString());
        whatYear.setImmediate(true);
        forceRangeToBeWithinACalendarYear.addValueChangeListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = -2031199434445240881L;

            @Override
            public void valueChange(final Property.ValueChangeEvent event) {
                boolean b = forceRangeToBeWithinACalendarYear.getValue();
                drf.setForceRangeToBeWithinACalendarYear(b);
                whatYear.setEnabled(b);
                allowUserToChangeYear.setEnabled(b);
            }
        });
        allowUserToChangeYear = new CheckBox("Allow User to Change Year", false);
        allowUserToChangeYear.setImmediate(true);
        allowUserToChangeYear.addValueChangeListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = -2031199434445240881L;

            @Override
            public void valueChange(final Property.ValueChangeEvent event) {
                drf.setAllowUserToChangeYear(allowUserToChangeYear.getValue());
            }
        });
        whatYear.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if ("".equals(whatYear.getValue())) {
                    forceRangeToBeWithinACalendarYear.setValue(false);
                    return;
                }
                try {
                    int y = Integer.parseInt(whatYear.getValue());
                    try {
                        drf.setRequiredCalendarYear(y);
                    } catch (Validator.InvalidValueException ive) {
                    }
                } catch (NumberFormatException nfe) {
                    Notification.show("Invalid integer value", Notification.Type.WARNING_MESSAGE);
                }
            }
        });
        whatYear.setConverter(new Converter<String, String>() {
            @Override
            public String convertToModel(String value, Class<? extends String> targetType,
                                         Locale locale) throws ConversionException {
                return value;
            }

            @Override
            public String convertToPresentation(String value, Class<? extends String> targetType,
                                                Locale locale) throws ConversionException {
                if ("-1".equals(value)) {
                    return "";
                }
                return value;
            }

            @Override
            public Class<String> getModelType() {
                return String.class;
            }

            @Override
            public Class<String> getPresentationType() {
                return String.class;
            }
        });
        minYear = new TextField("Minimum Allowed Year");
        minYear.setImmediate(true);
        minYear.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if ("".equals(minYear.getValue())) {
                    drf.setMinYear(-1);
                    validate();
                    return;
                }
                try {
                    int y = Integer.parseInt(minYear.getValue());
                    if (!"".equals(maxYear.getValue())) {
                        int maxy = Integer.parseInt(maxYear.getValue());
                        if (maxy < y) {
                            maxYear.setValue(String.valueOf(y));
                        }
                    }
                    drf.setMinYear(y);
                    validate();
                } catch (NumberFormatException nfe) {
                    Notification.show("Invalid integer value", Notification.Type.WARNING_MESSAGE);
                }
            }
        });
        maxYear = new TextField("Maximum Allowed Year");
        maxYear.setImmediate(true);
        maxYear.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if ("".equals(maxYear.getValue())) {
                    drf.setMaxYear(-1);
                    validate();
                    return;
                }
                try {
                    int y = Integer.parseInt(maxYear.getValue());
                    if (!"".equals(minYear.getValue())) {
                        int miny = Integer.parseInt(minYear.getValue());
                        if (miny > y) {
                            minYear.setValue(String.valueOf(y));
                        }
                    }
                    drf.setMaxYear(y);
                    validate();
                } catch (NumberFormatException nfe) {
                    Notification.show("Invalid integer value", Notification.Type.WARNING_MESSAGE);
                }
            }
        });
        drf.getYearField().addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Integer y = drf.getYearField().getValue();
                if (-1 != y) {
                    whatYear.setValue(y.toString());
                }
            }
        });

        options.addComponent(headerLabel);
        options.addComponent(verticalSpacer);
        options.addComponent(forceRangeToBeWithinACalendarYear);
        options.addComponent(whatYear);
        options.addComponent(allowUserToChangeYear);
        options.addComponent(minYear);
        options.addComponent(maxYear);

        // add to window
        final HorizontalLayout dateRangeFieldAndOptions = new HorizontalLayout();
        dateRangeFieldAndOptions.setSpacing(true);
        dateRangeFieldAndOptions.setMargin(true);
        VerticalLayout fieldAndButton = new VerticalLayout();
        fieldAndButton.setSpacing(true);
        fieldAndButton.addComponent(drf);
        fieldAndButton.addComponent(submit);
        dateRangeFieldAndOptions.addComponent(fieldAndButton);
        final Label horizontalSpacer = new Label();
        horizontalSpacer.setWidth("30px");
        dateRangeFieldAndOptions.addComponent(horizontalSpacer);
        dateRangeFieldAndOptions.addComponent(options);
        setContent(dateRangeFieldAndOptions);
    }

    private void validate() {
        try {
            // we call this to trigger the error marker on the fields
            drf.validate();
        } catch (Validator.InvalidValueException ive) {
        }
    }
}
