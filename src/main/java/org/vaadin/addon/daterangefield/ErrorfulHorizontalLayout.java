package org.vaadin.addon.daterangefield;

import com.vaadin.data.Validator;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;

import java.util.ArrayList;
import java.util.List;

public class ErrorfulHorizontalLayout extends HorizontalLayout implements ErrorfulFieldLayout {

    private List<Field<?>> fieldList = new ArrayList<Field<?>>();
    private String errorStyleName = "";

    @Override
    public void addComponent(Component c) {
        if (c instanceof Field) {
            if (!fieldList.contains(c)) {
                ErrorfulUtil.addErrorChangeListener((Field) c, this);
                fieldList.add((Field) c);
            }
        }
        super.addComponent(c);
    }

    @Override
    public List<Field<?>> getFields() {
        return fieldList;
    }

    @Override
    public Validator.InvalidValueException highlightInvalidFields() {
        return ErrorfulUtil.highlightInvalidFields(getFields(), this);
    }

    @Override
    public void discardInvalidBufferedValues() {
        ErrorfulUtil.discardInvalidBufferedValues(getFields());
    }

    @Override
    public void setErrorStyleName(String styleName) {
        this.errorStyleName = styleName;
    }

    @Override
    public String getErrorStyleName() {
        return errorStyleName;
    }

}
