package org.vaadin.addon.daterangefield;

import com.vaadin.data.Validator;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;

import java.util.Collection;

public interface ErrorfulFieldLayout extends Layout {

    @Override
    public void addComponent(Component c);

    public Collection<Field<?>> getFields();

    public Validator.InvalidValueException highlightInvalidFields();

    public void discardInvalidBufferedValues();

    public void setErrorStyleName(String styleName);

    public String getErrorStyleName();
}
